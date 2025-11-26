package uz.zero.shopapp

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

interface CategoryService {
    fun getAllCategories(pageable: Pageable): Page<CategoryResponse>
    fun getOne(id: Long): CategoryResponse
    fun add(request: CategoryRequest)
}

@Service
class CategoryServiceImpl(
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository,
) : CategoryService {
    override fun getAllCategories(pageable: Pageable): Page<CategoryResponse> {
        productRepository.findAll().map {
            ProductResponse(it.id!!, it.name.localized(), )
        }
        return categoryRepository.findAll(pageable).map {
            CategoryResponse.toAdminResponse(it)
        }
    }

    override fun getOne(id: Long): CategoryResponse {
        return categoryRepository.findByIdAndDeletedFalse(id)?.let {
            CategoryResponse.toAdminResponse(it)
        } ?: throw CategoryNotFoundException()
    }

    override fun add(request: CategoryRequest) {
        categoryRepository.save(Category(
            request.name,
            request.order,
            request.description
        ))
    }

}

