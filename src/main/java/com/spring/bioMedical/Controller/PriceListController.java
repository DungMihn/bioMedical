package com.spring.bioMedical.Controller;

import com.spring.bioMedical.entity.PriceList;
import com.spring.bioMedical.service.PriceListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/admin-super/prices")
public class PriceListController {

    @Autowired
    private PriceListService priceListService;

    // Danh sách bảng giá
    @GetMapping
    public String listPrices(@RequestParam(value = "q", required = false) String keyword,
            Model model) {
        List<PriceList> prices = priceListService.searchByName(keyword);
        model.addAttribute("prices", prices);
        model.addAttribute("keyword", keyword);
        return "admin-super/price-list";   // TODO: tạo Thymeleaf view này
    }

    // Form tạo mới
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("priceItem", new PriceList());
        model.addAttribute("formTitle", "Tạo mục bảng giá");
        return "admin-super/price-form";   // TODO: tạo view
    }

    // Form chỉnh sửa
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id,
            Model model,
            RedirectAttributes ra) {
        return priceListService.findById(id)
                .map(price -> {
                    model.addAttribute("priceItem", price);
                    model.addAttribute("formTitle", "Chỉnh sửa mục bảng giá");
                    return "admin-super/price-form";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("error", "Không tìm thấy mục bảng giá.");
                    return "redirect:/admin-super/prices";
                });
    }

    // Lưu (create + update)
    @PostMapping("/save")
    public String savePrice(@Valid @ModelAttribute("priceItem") PriceList priceList,
            BindingResult bindingResult,
            RedirectAttributes ra,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formTitle",
                    priceList.getPriceId() == null ? "Tạo mục bảng giá" : "Chỉnh sửa mục bảng giá");
            return "admin-super/price-form";
        }

        priceListService.save(priceList);
        ra.addFlashAttribute("success", "Lưu bảng giá thành công.");
        return "redirect:/admin-super/prices";
    }

    // Xóa
    @PostMapping("/delete/{id}")
    public String deletePrice(@PathVariable("id") Long id,
            RedirectAttributes ra) {
        priceListService.deleteById(id);
        ra.addFlashAttribute("success", "Đã xóa mục bảng giá.");
        return "redirect:/admin-super/prices";
    }
}
