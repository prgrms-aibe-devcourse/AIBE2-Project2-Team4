package com.example.portpilot.domain.board.board;

import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @Transactional
    public void create(BoardRequestDto dto, User user) {
        Board board = dto.toEntity();
        board.setUser(user);
        boardRepository.save(board);
    }

    // BoardService.java
    public Page<BoardResponseDto> getBoardsFiltered(String jobType, String techStack, String keyword, Pageable pageable) {
        jobType = (jobType == null || jobType.isBlank()) ? null : jobType;
        techStack = (techStack == null || techStack.isBlank()) ? null : techStack;
        keyword = (keyword == null || keyword.isBlank()) ? null : keyword;

        Page<Board> boards = boardRepository.findByFilter(jobType, techStack, keyword, pageable);
        return boards.map(BoardResponseDto::fromEntity);
    }



    @Transactional
    public BoardResponseDto getById(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));
        return BoardResponseDto.fromEntity(board);
    }

    @Transactional
    public BoardResponseDto viewBoard(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));
        board.setViewCount(board.getViewCount() + 1);
        return BoardResponseDto.fromEntity(board);
    }

    @Transactional
    public void updateBoard(Long id, BoardRequestDto dto) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));
        board.setTitle(dto.getTitle());
        board.setContent(dto.getContent());
        board.setJobType(dto.getJobType());
        board.setTechStack(dto.getTechStack());
    }

    @Transactional
    public void deleteBoard(Long id) {
        boardRepository.deleteById(id);
    }

    public Page<BoardResponseDto> getMyBoards(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email);
        Page<Board> boards = boardRepository.findByUser(user, pageable);
        return boards.map(BoardResponseDto::fromEntity);
    }

}
