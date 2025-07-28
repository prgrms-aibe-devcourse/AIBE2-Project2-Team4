package com.example.portpilot.domain.portfolio.controller;

import com.example.portpilot.domain.portfolio.dto.PortfolioRequest;
import com.example.portpilot.domain.portfolio.dto.PortfolioResponse;
import com.example.portpilot.domain.portfolio.service.PortfolioService;
import com.example.portpilot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/portfolio")
public class PortfolioViewController {

    private final PortfolioService portfolioService;
    private final UserRepository userRepository;

    @GetMapping("/ping")
    @ResponseBody
    public String ping() {
        return "pong";
    }

    @GetMapping({"", "/explore"})
    public String list(
            @RequestParam(value = "q", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            Model model
    ) {
        var portPage = portfolioService.searchPortfolios(keyword, PageRequest.of(page, size));
        model.addAttribute("portfolios", portPage.getContent());
        model.addAttribute("page", portPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("active", "portfolio");
        return "portfolio/portfolioview";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("portfolioForm", new PortfolioRequest());
        model.addAttribute("active", "portfolio");
        return "portfolio/form";
    }

    @PostMapping("/new")
    public String create(
            @Valid @ModelAttribute("portfolioForm") PortfolioRequest req,
            BindingResult binding,
            @RequestParam("files") MultipartFile[] files,
            Principal principal,
            RedirectAttributes rttr
    ) {
        if (binding.hasErrors()) {
            return "portfolio/form";
        }
        var user = userRepository.findByEmail(principal.getName());
        Long id = portfolioService.createPortfolioWithFiles(user.getId(), req, files);
        rttr.addFlashAttribute("msg", "등록 성공");
        return "redirect:/portfolio/" + id;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        PortfolioResponse dto = portfolioService.getPortfolio(id);
        model.addAttribute("portfolio", dto);
        model.addAttribute("active", "portfolio");
        return "portfolio/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        PortfolioResponse dto = portfolioService.getPortfolio(id);
        model.addAttribute("portfolioForm", PortfolioRequest.fromResponse(dto));
        model.addAttribute("active", "portfolio");
        return "portfolio/form";
    }

    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("portfolioForm") PortfolioRequest req,
            BindingResult binding,
            @RequestParam("files") MultipartFile[] files,
            RedirectAttributes rttr
    ) {
        if (binding.hasErrors()) {
            return "portfolio/form";
        }
        portfolioService.updatePortfolioWithFiles(id, req, files);
        rttr.addFlashAttribute("msg", "수정 성공");
        return "redirect:/portfolio/" + id;
    }
}