package com.example.hiberentitygraph.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NamedEntityGraphs(
        {
                @NamedEntityGraph(name = "post-entity-graph", attributeNodes =
                        {
                                @NamedAttributeNode(value = "images")
                        }
                )
        }
)
@Setter
@Getter
@Entity
@ToString(exclude = "images")
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<Image> images;

    @ElementCollection//(fetch = FetchType.EAGER)
    private Set<String> tags = new HashSet<>();

    public Post(String title) {
        this.title = title;
    }

    public void addImage(Image image) {
        if (this.images == null) {
            this.images = new ArrayList<>();
        }
        this.images.add(image);
        image.setPost(this);
    }

}
