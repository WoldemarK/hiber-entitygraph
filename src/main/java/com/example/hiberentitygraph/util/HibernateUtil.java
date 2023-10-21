package com.example.hiberentitygraph.util;

import lombok.NoArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.util.function.Consumer;
import java.util.function.Function;

@NoArgsConstructor
public class HibernateUtil {
    private static SessionFactory sessionFactory;


    static {
        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure()
                .build();
        try {
            sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
        } catch (Exception e) {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void doInHibernate(Consumer<Session> callable) {
        Transaction txn = null;
        try (Session session = getSessionFactory().openSession()) {
            txn = session.beginTransaction();
            callable.accept(session);
            txn.commit();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static <T> T doInHibernate(Function<Session, T> callable) {
        T result = null;
        Transaction txn = null;
        try (Session session = getSessionFactory().openSession()) {
            txn = session.beginTransaction();
            result = callable.apply(session);
            txn.commit();
        } catch (Throwable t) {
            assert txn != null;
            txn.rollback();
        }
        return result;
    }
}
