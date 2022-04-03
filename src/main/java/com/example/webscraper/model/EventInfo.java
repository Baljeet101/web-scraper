package com.example.webscraper.model;

import com.sun.istack.NotNull;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Table(name = "event_info")
public class EventInfo  {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer id;

    @Column(name = "Event_Name")
    @NotNull
    String eventName;

    @Column(name = "Event_Location")
    @NotNull
    String eventLocation;

    @Column(name = "Start_Date")
    @NotNull
    Date startDate;

    @Column(name = "End_Date")
    @NotNull
    Date endDate;

//    @JsonBackReference
//    @ManyToOne(optional = false, fetch = FetchType.LAZY)
//    @JoinColumn(name = "scrapWebsite_id",  referencedColumnName = "id")
//    ScrapWebsite scrapWebsite;

}
