package com.example.webscraper.model;

import com.sun.istack.NotNull;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@Table(name="websites")
public class ScrapWebsite  {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer id;

    @Column(name = "url")
    @NotNull
    String url;

    @Column(name = "website_name",unique=true)
    @NotNull
    String websiteName;

//    @JsonManagedReference
//    @OneToMany(mappedBy="scrapWebsite", cascade = CascadeType.ALL,fetch=FetchType.LAZY)
    @OneToMany(targetEntity=EventInfo.class)
    List<EventInfo> eventList;
}
