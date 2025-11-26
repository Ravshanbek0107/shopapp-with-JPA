package uz.zero.shopapp

import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/category")
class CategoryController(
    private val categoryService: CategoryService
) {

    @GetMapping
    fun all(pageable: Pageable) = categoryService.getAllCategories(pageable)

    @PostMapping
    fun create(@RequestBody request: CategoryRequest) = categoryService.add(request)


    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long) = categoryService.getOne(id)

}
