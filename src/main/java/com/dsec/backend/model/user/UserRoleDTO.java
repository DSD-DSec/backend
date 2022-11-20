package com.dsec.backend.model.user;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;
import com.dsec.backend.entity.Role;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonRootName(value = "userRole")
@JsonInclude(Include.NON_NULL)
@Relation(collectionRelation = "userRoles")
public class UserRoleDTO extends RepresentationModel<UserRoleDTO> {
    private Integer id;
    private Role roleName;
}
