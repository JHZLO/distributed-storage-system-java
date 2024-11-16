package org.example.repository;

import org.example.domain.Memo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemoRepository extends JpaRepository<Memo, Long> {
}
