package com.securestay.repository;

import com.securestay.model.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List; // Don't forget this import!

@Repository
public interface VisitorRepository extends JpaRepository<Visitor, Long> {

    // This tells Spring Data JPA to automatically create the SQL query
    List<Visitor> findByStatus(String status);
}