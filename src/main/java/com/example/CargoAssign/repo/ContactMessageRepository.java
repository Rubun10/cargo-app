package com.example.CargoAssign.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CargoAssign.Model.ContactMessage;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
}
