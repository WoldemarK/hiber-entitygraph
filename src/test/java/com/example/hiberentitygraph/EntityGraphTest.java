package com.example.hiberentitygraph;

import com.example.hiberentitygraph.model.Image;
import com.example.hiberentitygraph.model.Post;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityGraph;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.example.hiberentitygraph.util.HibernateUtil.doInHibernate;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EntityGraphTest {
    @BeforeAll
    private static void createPosts() {
        doInHibernate(session -> {
            for (int i = 0; i < 5; i++) {
                Post post = new Post("topic" + i);
                Image image1 = new Image("url1_" + i);
                Image image2 = new Image("url2_" + i);
                post.addImage(image1);
                post.addImage(image2);

                Set<String> tags = new HashSet<>(Arrays.asList("red", "green", "blue", "orange", "white"));
                post.setTags(tags);
                session.merge(post);
            }
        });
    }

    @Test
    @DisplayName("если не использовать EntityGraph, коллекции не загружаются")
    public void givenDefaultFetchStrategy_whenFind_thenCollectionsAreLazy() {
        doInHibernate(session -> {
            Optional<Post> post = Optional.ofNullable(session.find(Post.class, 1l));
            assertNotNull(post);
            assertTrue(post.isPresent());
        });
    }

    @Test
    @DisplayName("если использовать EntityGraph, загружаются images")
    public void givenEntityGraph_whenFind_thenImagesAreEager() {
        doInHibernate(session -> {
            Map<String, Object> properties = new HashMap<>();
            properties.put("javax.persistence.fetchgraph", session.getEntityGraph("post-entity-graph"));
            Optional<Post> post = Optional.ofNullable(session.find(Post.class, 1l, properties));
            assertNotNull(post);
            assertTrue(post.isPresent());
        });
    }

    @Test
    @DisplayName("если создать динамически EntityGraph, загружаются tags")
    public void givenDynamicEntityGraph_whenFind_thenTagsAreEager() {
        doInHibernate(session -> {
            Map<String, Object> properties = new HashMap<>();
            EntityGraph<Post> postGraph = session.createEntityGraph(Post.class);
            postGraph.addAttributeNodes("tags");
            properties.put("javax.persistence.fetchgraph", postGraph);
            Optional<Post> post = Optional.ofNullable(session.find(Post.class, 1l, properties));
            assertNotNull(post);
            assertTrue(post.isPresent());
        });
    }

    @Test
    @DisplayName("EntityGraph c Query тоже работает")
    public void givenEntityGraph_whenQuery_thenImagesAreEager() {
        doInHibernate(session -> {
            EntityGraph<?> entityGraph = session.getEntityGraph("post-entity-graph");
            Optional<Post> post = Optional.ofNullable(session.createQuery("select p from Post p where p.id = :id", Post.class)
                    .setParameter("id", 1l)
                    .setHint("javax.persistence.fetchgraph", entityGraph)
                    .getSingleResult());
            assertNotNull(post);
            assertTrue(post.isPresent());
        });
    }

    @Test
    @DisplayName("если создать loadgraph и раскомментировать (fetch = FetchType.EAGER), загружаются tags")
    public void givenLoadGraph_whenFind_thenTagsAreEager() {
        doInHibernate(session -> {
            Map<String, Object> properties = new HashMap<>();
            EntityGraph<Post> postGraph = session.createEntityGraph(Post.class);
            properties.put("javax.persistence.loadgraph", postGraph);
            Optional<Post> post = Optional.ofNullable(session.find(Post.class, 1l, properties));
            assertNotNull(post);
            assertTrue(post.isPresent());
        });
    }

}
