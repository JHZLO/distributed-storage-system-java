package org.example.controller;

import org.example.domain.Memo;
import org.example.service.MemoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class MemoControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Mock
    private MemoService memoService;

    @InjectMocks
    private MemoController memoController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(memoController).build();
    }

    @Test
    public void testGetAllMemos() throws Exception {
        Memo memo1 = new Memo(1L, "Title 1", "Body 1");
        Memo memo2 = new Memo(2L, "Title 2", "Body 2");
        List<Memo> memos = Arrays.asList(memo1, memo2);

        when(memoService.getAllMemos()).thenReturn(memos);

        mockMvc.perform(get("/notes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Title 1"))
                .andExpect(jsonPath("$[0].body").value("Body 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Title 2"))
                .andExpect(jsonPath("$[1].body").value("Body 2"));
    }

    @Test
    public void testGetMemoById() throws Exception {
        Memo memo = new Memo(1L, "Title 1", "Body 1");

        when(memoService.getMemoById(1L)).thenReturn(Optional.of(memo));

        mockMvc.perform(get("/notes/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Title 1"))
                .andExpect(jsonPath("$.body").value("Body 1"));
    }

    @Test
    public void testGetMemoById_NotFound() throws Exception {
        when(memoService.getMemoById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/notes/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateMemo() throws Exception {
        Memo memo = new Memo(null, "New Title", "New Body");
        Memo createdMemo = new Memo(1L, "New Title", "New Body");

        when(memoService.createMemo(any(Memo.class))).thenReturn(createdMemo);

        mockMvc.perform(post("/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"New Title\", \"body\":\"New Body\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.body").value("New Body"));
    }

    @Test
    public void testUpdateMemo() throws Exception {
        Memo updatedMemo = new Memo(1L, "Updated Title", "Updated Body");

        when(memoService.updateMemo(anyLong(), any(Memo.class))).thenReturn(updatedMemo);

        mockMvc.perform(put("/notes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated Title\", \"body\":\"Updated Body\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.body").value("Updated Body"));
    }

    @Test
    public void testUpdateMemo_NotFound() throws Exception {
        when(memoService.updateMemo(anyLong(), any(Memo.class))).thenThrow(new RuntimeException());

        mockMvc.perform(put("/notes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated Title\", \"body\":\"Updated Body\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteMemo() throws Exception {
        doNothing().when(memoService).deleteMemo(1L);

        mockMvc.perform(delete("/notes/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"msg\": \"OK\"}"));
    }

    @Test
    public void testDeleteMemo_NotFound() throws Exception {
        doThrow(new RuntimeException()).when(memoService).deleteMemo(1L);

        mockMvc.perform(delete("/notes/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("{\"msg\": \"ERROR\"}"));
    }
}
