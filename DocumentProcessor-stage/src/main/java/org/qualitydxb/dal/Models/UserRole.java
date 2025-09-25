package org.qualitydxb.dal.Models;

import jakarta.persistence.*;

@Entity
@Table(name = "userrole", schema = "qualitydxb")
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "roleid")
    public Integer roleId; // Primary Key

    @Column(name = "rolename")
    public String roleName; // Role Name

    @Column(name = "description")
    public String description; // Description
}


