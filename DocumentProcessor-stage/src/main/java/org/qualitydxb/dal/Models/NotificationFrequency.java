package org.qualitydxb.dal.Models;

import jakarta.persistence.*;

@Entity
@Table(name = "notificationfrequency", schema = "qualitydxb")
public class NotificationFrequency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notificationfrequencyid")
    public Integer notificationFrequencyId; // Primary Key

    @Column(name = "frequency")
    public String frequency; // Frequency Type

    @Column(name = "description")
    public String description; // Description

}
