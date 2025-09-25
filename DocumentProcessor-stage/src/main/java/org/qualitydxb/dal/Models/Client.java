package org.qualitydxb.dal.Models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "clients", schema = "qualitydxb")
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clientid")
    public Integer clientId;

    @Column(name = "clientname")
    public String clientName;

    @Column(name = "createdat")
    public LocalDateTime createdAt;
}
