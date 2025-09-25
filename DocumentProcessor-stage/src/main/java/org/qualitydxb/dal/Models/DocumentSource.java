package org.qualitydxb.dal.Models;

import jakarta.persistence.*;

@Entity
@Table(name = "documentsource", schema = "qualitydxb")
public class DocumentSource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sourceid")
    public Integer sourceId; // Primary Key

    @Column(name = "sourcename")
    public String sourceName; // Source Name

    @Column(name = "description")
    public String description; // Description
}
