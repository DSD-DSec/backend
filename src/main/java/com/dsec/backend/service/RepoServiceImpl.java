package com.dsec.backend.service;

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
    public RepoDTO createRepo(String fullRepoName, Jwt jwt) {
        if (repoRepository.existsByFullName(fullRepoName)) {
            throw new EntityAlreadyExistsException();
        }

        Repo repo = githubClientService.getRepo(fullRepoName, jwt).block();

        log.info("Repo: {}", repo);
        UserEntity user = UserPrincipal.fromClaims(jwt.getClaims()).getUserEntity();

        user = userService.fetch(user.getId());

        repo.getUsers().add(user);

        String url = githubClientService.createWebHook(fullRepoName, jwt).block();

        repo.setHookUrl(url);

        RepoDTO repoDto = new RepoDTO();

        BeanUtils.copyProperties(repoRepository.save(repo), repoDto);

        return repoDto;
    }

    @Override
    public Repo fetch(long id) {
        return repoRepository.findById(id)
                .orElseThrow(() -> new EntityMissingException(Repo.class, id));
    }

    @Override
    public void triggerHook(long id, Jwt jwt) {
        Repo repo = fetch(id);
        githubClientService.triggerHook(repo.getHookUrl(), jwt);
    }

}
