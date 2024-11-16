package org.example.service;

import org.example.domain.Memo;
import org.example.repository.MemoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MemoService {
    private final MemoRepository memoRepository;

    public MemoService(MemoRepository memoRepository) {
        this.memoRepository = memoRepository;
    }

    public List<Memo> getAllMemos() {
        return memoRepository.findAll();
    }

    public Optional<Memo> getMemoById(Long id) {
        return memoRepository.findById(id);
    }

    public Memo createMemo(Memo memo) {
        return memoRepository.save(memo);
    }

    public Memo updateMemo(Long id, Memo newMemo) {
        return memoRepository.findById(id).map(memo -> {
            memo.setTitle(newMemo.getTitle());
            memo.setBody(newMemo.getBody());
            return memoRepository.save(memo);
        }).orElseThrow(() -> new RuntimeException("Memo not found"));
    }

    public Memo partialUpdateMemo(Long id, Memo partialMemo) {
        return memoRepository.findById(id).map(memo -> {
            if (partialMemo.getTitle() != null) {
                memo.setTitle(partialMemo.getTitle());
            }
            if (partialMemo.getBody() != null) {
                memo.setBody(partialMemo.getBody());
            }
            return memoRepository.save(memo);
        }).orElseThrow(() -> new RuntimeException("Memo not found"));
    }

    public void deleteMemo(Long id) {
        if (!memoRepository.existsById(id)) {
            throw new RuntimeException("Memo not found");
        }
        memoRepository.deleteById(id);
    }
}
