package uz.zero.shopapp

import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController




@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {

    @PostMapping
    fun createUser(
        @RequestParam fullname: String,
        @RequestParam username: String
    ) = userService.createUser(fullname, username)

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long) = userService.getUserById(id)

    @GetMapping("/username/{username}")
    fun getUserByUsername(@PathVariable username: String) = userService.getUserByUsername(username)

    @GetMapping("/{username}/balance")
    fun getUserBalance(@PathVariable username: String) = userService.getUserBalance(username)

}


@RestController
@RequestMapping("/api/payments")
class UserPaymentController(
    private val userPaymentService: UserPaymentService
) {

    @PostMapping("/add-balance")
    fun addBalance(@RequestBody request: UserPaymentRequest) = userPaymentService.addBalance(request)

    @GetMapping("/user/{userId}/history")
    fun getUserPaymentHistory(@PathVariable userId: Long) = userPaymentService.getUserPaymentHistory(userId)
}


@RestController
@RequestMapping("/api/transactions")
class TransactionController(
    private val transactionService: TransactionService
) {

    @PostMapping("/buy")
    fun buy(@RequestBody request: BuyRequest) = transactionService.processBuy(request)

    @GetMapping("/user/{userId}/history")
    fun getUserBuyHistory(@PathVariable userId: Long) = transactionService.getUserBuyHistory(userId)

    @GetMapping("/{transactionId}/items")
    fun getTransactionItems(@PathVariable transactionId: Long) = transactionService.getTransactionItems(transactionId)

    @GetMapping("/{transactionId}/products")
    fun getTransactionProducts(@PathVariable transactionId: Long) = transactionService.getTransactionProducts(transactionId)

    @GetMapping("/admin/all")
    fun getAllTransactions() = transactionService.getAllTransactions()
}



@RestController
@RequestMapping("/api/products")
class ProductController(
    private val productService: ProductService
) {

    @GetMapping("/search")
    fun searchProducts(@RequestParam keyword: String) = productService.searchProducts(keyword)

    @GetMapping("/category/{categoryId}")
    fun getProductsByCategory(@PathVariable categoryId: Long) = productService.getProductsByCategory(categoryId)

    @GetMapping("/available")
    fun getAvailableProducts() = productService.getAllAvailableProducts()

    @DeleteMapping("/{id}")
    fun deleteProduct(@PathVariable id: Long) {
        productService.deleteProduct(id)
    }

}


















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
