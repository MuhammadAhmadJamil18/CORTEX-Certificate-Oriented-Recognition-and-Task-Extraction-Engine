package org.qualitydxb.dal.Models;

import jakarta.persistence.*;

@Entity
@Table(name = "notificationtype", schema = "qualitydxb")
public class NotificationType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notificationtypeid")
    public Integer notificationTypeId; // Primary Key

    @Column(name = "typename")
    public String typeName; // Type Name

    @Column(name = "description")
    public String description; // Description
}
