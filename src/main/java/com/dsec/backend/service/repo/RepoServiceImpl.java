package com.dsec.backend.service.repo;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.dsec.backend.entity.Repo;
import com.dsec.backend.entity.ToolEntity;
import com.dsec.backend.entity.ToolRepo;
import com.dsec.backend.entity.UserEntity;
import com.dsec.backend.entity.UserRepo;
import com.dsec.backend.exception.EntityAlreadyExistsException;
import com.dsec.backend.exception.EntityMissingException;
import com.dsec.backend.exception.ForbidenAccessException;
import com.dsec.backend.model.github.GetWebhookDTO;
import com.dsec.backend.model.github.RepoDTO;
import com.dsec.backend.model.repo.CreateRepoDTO;
import com.dsec.backend.model.tools.RepoToolUpdateDTO;
import com.dsec.backend.repository.RepoRepository;
import com.dsec.backend.repository.ToolRepoRepository;
import com.dsec.backend.repository.UserRepoRepository;
import com.dsec.backend.security.UserPrincipal;
import com.dsec.backend.service.github.GithubClientService;
import com.dsec.backend.service.tool.ToolService;
import com.dsec.backend.service.user.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RepoServiceImpl implements RepoService {

    private final RepoRepository repoRepository;
    private final UserService userService;
    private final GithubClientService githubClientService;
    private final UserRepoRepository userRepoRepository;
    private final ToolService toolService;

    private final ToolRepoRepository toolRepoRepository;

    @Override
    public Page<Repo> getRepos(Pageable pageable) {
        return repoRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Repo createRepo(String fullName, CreateRepoDTO createRepoDTO, Jwt jwt) {
        // Repository already existing
        if (repoRepository.existsByFullName(fullName)) {
            throw new EntityAlreadyExistsException();
        }

        // New repo is created using GitHub client service
        RepoDTO repoDto = githubClientService.getRepo(fullName, jwt).block();

        List<GetWebhookDTO> hooks = githubClientService.getWebhooks(fullName, jwt).block();
        Optional<GetWebhookDTO> hook = githubClientService.getExistingHook(hooks);

        Repo repo = new Repo();
        BeanUtils.copyProperties(Objects.requireNonNull(repoDto), repo);
        repo.setGithubId(repoDto.getId());

        // User is fetched from the jwt token, it is also the owner of the repo
        UserEntity jwtUser = UserPrincipal.fromClaims(jwt.getClaims()).getUserEntity();
        UserEntity user = userService.fetch(jwtUser.getId());

        // Setting repository parameters from DTO
        BeanUtils.copyProperties(createRepoDTO, repo);

        String hookUrl = hook.map(GetWebhookDTO::getUrl)
                .orElseGet(() -> githubClientService.createWebHook(fullName, jwt).block());
        repo.setHookUrl(hookUrl);

        repo = repoRepository.save(repo);

        UserRepo userRepo = new UserRepo(null, user, repo, true);
        userRepoRepository.save(userRepo);
        toolService.priorityMatrix(repo);

        return repo;
    }

    @Override
    public Repo deleteRepo(Repo repo, Jwt jwt) {
        UserEntity userJwt = UserPrincipal.fromClaims(jwt.getClaims()).getUserEntity();

        if (!isOwner(repo, userJwt))
            throw new ForbidenAccessException("Invalid repo deletion.");

        String hook = repo.getHookUrl();

        repoRepository.delete(repo);

        if (hook.matches("^.+api.github.com.+")) {
            githubClientService.deleteWebhook(hook, jwt);
        }

        return repo;
    }

    @Override
    public Repo updateRepo(long id, Repo repo, CreateRepoDTO createRepoDTO, Jwt jwt) {
        UserEntity userJwt = UserPrincipal.fromClaims(jwt.getClaims()).getUserEntity();

        if (!isOwner(repo, userJwt))
            throw new ForbidenAccessException("Invalid repo update.");

        repo.setRepoName(createRepoDTO.getRepoName());
        repo.setDescription(createRepoDTO.getDescription());
        repo.setType(createRepoDTO.getType());
        repo.setDomain(createRepoDTO.getDomain());
        repo.setUserData(createRepoDTO.getUserData());
        repo.setSecurity(createRepoDTO.getSecurity());
        repo.setPrivacy(createRepoDTO.getPrivacy());

        return repoRepository.save(repo);
    }

    @Override
    public List<ToolEntity> updateRepoTools(long id, RepoToolUpdateDTO repoToolUpdateDTO, Jwt jwt) {
        UserEntity userJwt = UserPrincipal.fromClaims(jwt.getClaims()).getUserEntity();

        Repo repo = fetch(id);

        if (!isOwner(repo, userJwt))
            throw new ForbidenAccessException("Invalid repo update.");

        Set<ToolEntity> toBeContained = new HashSet<>();
        for (Integer toolID : repoToolUpdateDTO.getTools()) {
            toBeContained.add(toolService.getToolByID(toolID));
        }

        Set<ToolEntity> contained = new HashSet<>();

        Iterator<ToolRepo> it = repo.getToolRepos().iterator();
        while (it.hasNext()) {
            ToolRepo tr = it.next();
            if (!toBeContained.contains(tr.getTool())) {
                toolRepoRepository.deleteById(tr.getId());
                it.remove();
            } else {
                contained.add(tr.getTool());
            }
        }

        final Repo cpy = repo;
        toBeContained.forEach(t -> {
            if (!contained.contains(t)) {
                ToolRepo tr = new ToolRepo(null, cpy, t);
                tr = toolRepoRepository.save(tr);
                cpy.getToolRepos().add(tr);
            }
        });

        repoRepository.save(repo);

        return getToolsByRepo(id, jwt);
    }

    @Override
    public Repo getById(long id, Jwt jwt) {

        // For now, only the owner of the repository is able to retrieve it
        // to do: all the team members must be able to retrieve the repository project
        // (if teams will be implemented)

        UserEntity userJwt = UserPrincipal.fromClaims(jwt.getClaims()).getUserEntity();

        Repo repo = fetch(id);

        if (!isOwner(repo, userJwt)) {
            throw new ForbidenAccessException("You have not the permission to access to this Repository.");
        }

        return repoRepository.findById(id)
                .orElseThrow(() -> new EntityMissingException(Repo.class, id));
    }

    @Override
    public Repo fetch(long id) {

        return repoRepository.findById(id)
                .orElseThrow(() -> new EntityMissingException(Repo.class, id));
    }

    @Override
    public Repo fetchByGithubId(long githubId) {
        return repoRepository.findByGithubId(githubId)
                .orElseThrow(() -> new EntityMissingException(Repo.class, githubId));
    }

    @Override
    public void triggerHook(long id, Jwt jwt) {
        Repo repo = fetch(id);
        githubClientService.triggerHook(repo.getHookUrl(), jwt);
    }

    @Override
    public List<ToolEntity> getToolsByRepo(long id, Jwt jwt) {
        Repo repo = getById(id, jwt);

        return repo.getToolRepos().stream().map(ToolRepo::getTool).collect(Collectors.toList());
    }

    private boolean isOwner(Repo repo, UserEntity userJwt) {
        return repo.getUserRepos().stream().filter(UserRepo::getIsOwner)
                .allMatch(ur -> ur.getUser().getId().equals(userJwt.getId()));
    }

}
