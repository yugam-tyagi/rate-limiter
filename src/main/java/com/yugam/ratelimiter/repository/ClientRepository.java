package com.yugam.ratelimiter.repository;

import com.yugam.ratelimiter.model.Client;

import java.util.Optional;

public interface ClientRepository {
    Optional<Client> findByClientId(String clientId);
}
