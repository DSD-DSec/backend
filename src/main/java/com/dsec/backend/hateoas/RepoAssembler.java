package com.dsec.backend.hateoas;

import com.dsec.backend.controller.RepoController;
import com.dsec.backend.entity.Repo;
import com.dsec.backend.model.repo.RepoUpdateDTO;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class RepoAssembler extends RepresentationModelAssemblerSupport<Repo, Repo> {

    public RepoAssembler() {
        super(RepoController.class, Repo.class);
    }

    @Override
    public Repo toModel(Repo repo) {


        Link link = linkTo(methodOn(RepoController.class).fetchRepo(repo.getId()))
                .withSelfRel().withType(HttpMethod.GET.name());
        repo.add(link);

        repo.add(Affordances
                .of(linkTo(methodOn(RepoController.class).deleteRepo(repo.getId(),
                        null)).withRel("delete")
                        .withType(HttpMethod.DELETE.name()))
                .afford(HttpMethod.DELETE)
                .withOutput(Repo.class)
                .withName("delete").toLink());

        repo.add(Affordances
                .of(linkTo(methodOn(RepoController.class).updateRepo(repo.getId(),
                        null, null)).withRel("update")
                        .withType(HttpMethod.PUT.name()))
                .afford(HttpMethod.PUT)
                .withInput(RepoUpdateDTO.class)
                .withOutput(Repo.class)
                .withName("update").toLink());

        return repo;
    }

}
