package com.jin.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.jin.domain.FailedMessage;

public interface FailedMessageRepository extends MongoRepository<FailedMessage, String> {

	public List<FailedMessage> findByRetried(boolean retried, Pageable pageable);
	
	public List<FailedMessage> findByMessageTypeAndRetried(String messageType, boolean retried, Pageable pageable);
}
