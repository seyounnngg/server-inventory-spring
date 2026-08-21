package com.example.serverinventory.repository;

import com.example.serverinventory.domain.Server;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ServerRepository extends JpaRepository<Server, Long> {

    @Query("select s from Server s left join fetch s.owner where s.deletedAt is null")
    List<Server> findAllByDeletedAtIsNull();

    @Query("select s from Server s left join fetch s.owner where s.id = :id and s.deletedAt is null")
    Optional<Server> findByIdAndDeletedAtIsNull(Long id);
}
