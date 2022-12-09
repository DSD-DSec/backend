package com.dsec.backend.service;

import com.dsec.backend.exception.ForbidenAccessException;
import com.dsec.backend.model.repo.RepoUpdateDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.dsec.backend.entity.Repo;
import com.dsec.backend.entity.UserEntity;
import com.dsec.backend.exception.EntityAlreadyExistsException;
import com.dsec.backend.exception.EntityMissingException;
import com.dsec.backend.model.github.RepoDTO;
import com.dsec.backend.repository.RepoRepository;
import com.dsec.backend.security.UserPrincipal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RepoServiceImpl implements RepoService {

    private final RepoRepository repoRepository;
    private final UserService userService;
    private final GithubClientService githubClientService;

    @Override
    public Page<Repo> getRepos(Pageable pageable) {
        return repoRepository.findAll(pageable);
    }

    @Override
    public Repo createRepo(RepoDTO parameters, Jwt jwt) {
        //Repository already existing
        if (repoRepository.existsByFullName(parameters.getFullName())) {
            throw new EntityAlreadyExistsException();
        }

        // New repo is created using GitHub client service
        Repo repo = githubClientService.getRepo(parameters.getFullName(), jwt).block();
        // User is fetched from the jwt token, it is also the owner of the repo
        UserEntity user = UserPrincipal.fromClaims(jwt.getClaims()).getUserEntity();

        // Setting repository parameters from DTO
        assert repo != null;
        repo.setRepoName(parameters.getRepoName());
        repo.setOwner(user.getId());
        repo.setDescription(parameters.getDescription());
        repo.setType(parameters.getType());
        repo.setDomain(parameters.getDomain());
        repo.setUserData(parameters.getUserData());
        repo.setSecurity(parameters.getSecurity());
        repo.setAvailability(parameters.getAvailability());

        user = userService.fetch(user.getId());
        repo.getUsers().add(user);
        String url = githubClientService.createWebHook(parameters.getFullName(), jwt).block();
        repo.setHookUrl(url);

        BeanUtils.copyProperties(repoRepository.save(repo), parameters);

        return repo;
    }

    @Override
    public Repo deleteRepo(Repo repo, Jwt jwt) {
        UserEntity userJwt = UserPrincipal.fromClaims(jwt.getClaims()).getUserEntity();

        if (!userJwt.getId().equals(repo.getOwner()))
            throw new ForbidenAccessException("Invalid repo deletion.");

        repoRepository.deleteById(repo.getId());

        return repo;
    }

    @Override
    public Repo updateRepo(long id, Repo repo, RepoUpdateDTO repoUpdateDTO, Jwt jwt) {
        UserEntity userJwt = UserPrincipal.fromClaims(jwt.getClaims()).getUserEntity();

        if (!userJwt.getId().equals(repo.getOwner()))
            throw new ForbidenAccessException("Invalid repo update.");

        repo.setFullName(repoUpdateDTO.getFullName());
        repo.setRepoName(repoUpdateDTO.getRepoName());
        repo.setDescription(repoUpdateDTO.getDescription());
        repo.setType(repoUpdateDTO.getType());
        repo.setDomain(repoUpdateDTO.getDomain());
        repo.setUserData(repoUpdateDTO.getUserData());
        repo.setSecurity(repoUpdateDTO.getSecurity());
        repo.setAvailability(repoUpdateDTO.getAvailability());


        return repoRepository.save(repo);
    }

    @Override
    public Repo getById(long id, Jwt jwt) {

        // For now, only the owner of the repository is able to retrieve it
        // TODO: all the team members must be able to retrieve the repository project (if teams will be implemented)

        UserEntity userJwt = UserPrincipal.fromClaims(jwt.getClaims()).getUserEntity();

        Optional<Repo> repo = repoRepository.findById(id);

        if(repo.isPresent()) {
            if (!userJwt.getId().equals(repo.get().getOwner()))
                throw new ForbidenAccessException("You have not the permission to access to this Repository.");
        }

        return repoRepository.findById(id)
                .orElseThrow(() -> new EntityMissingException(Repo.class, id));
    }

    @Override
    public Repo fetch(long id) {

        return repoRepository.findById(id)
                .orElseThrow(() -> new EntityMissingException(UserEntity.class, id));
    }

    @Override
    public void triggerHook(long id, Jwt jwt) {
        Repo repo = fetch(id);
        githubClientService.triggerHook(repo.getHookUrl(), jwt);
    }

}
