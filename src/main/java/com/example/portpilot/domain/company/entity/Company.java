
package com.example.portpilot.domain.company.entity;

import com.example.portpilot.global.common.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "company")
@Getter
@Setter
@NoArgsConstructor
public class Company extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "industry", length = 100)
    private String industry;

    @Column(name = "location", length = 500)
    private String location;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}