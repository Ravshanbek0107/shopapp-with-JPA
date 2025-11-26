package uz.zero.shopapp

import jakarta.persistence.EntityManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.findByIdOrNull
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.Optional


@NoRepositoryBean
interface BaseRepository<T : BaseEntity> : JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
    fun findByIdAndDeletedFalse(id: Long): T?
    fun trash(id: Long): T?
    fun trashList(ids: List<Long>): List<T?>
    fun findAllNotDeleted(): List<T>
    fun findAllNotDeleted(pageable: Pageable): Page<T>
}


class BaseRepositoryImpl<T : BaseEntity>(
    entityInformation: JpaEntityInformation<T, Long>, entityManager: EntityManager
) : SimpleJpaRepository<T, Long>(entityInformation, entityManager), BaseRepository<T> {

    val isNotDeletedSpecification = Specification<T> { root, _, cb -> cb.equal(root.get<Boolean>("deleted"), false) }

    override fun findByIdAndDeletedFalse(id: Long) = findByIdOrNull(id)?.run { if (deleted) null else this }

    @Transactional
    override fun trash(id: Long): T? = findByIdOrNull(id)?.run {
        deleted = true
        save(this)
    }

    override fun findAllNotDeleted(): List<T> = findAll(isNotDeletedSpecification)
    override fun findAllNotDeleted(pageable: Pageable): Page<T> = findAll(isNotDeletedSpecification, pageable)
    override fun trashList(ids: List<Long>): List<T?> = ids.map { trash(it) }
}

@Repository
interface UserRepository : BaseRepository<User> {
    fun findByUsername(username: String): Optional<User>
    fun existsByUsername(username: String): Boolean
}



@Repository
interface TransactionRepository : BaseRepository<Transaction> {
    fun findByUserIdAndDeletedFalse(userId: Long): List<Transaction>
    fun findByDeletedFalseOrderByDateDesc(): List<Transaction>

// to admin
    fun findAllByOrderByDateDesc(): List<Transaction>
}

@Repository
interface UserPaymentTransactionRepository : BaseRepository<UserPaymentTransaction> {
    fun findByUserId(userId: Long): List<UserPaymentTransaction>
    fun findAllByOrderByDateDesc(): List<UserPaymentTransaction>
}

@Repository
interface CategoryRepository : BaseRepository<Category> {}

@Repository
interface ProductRepository : BaseRepository<Product> {
    @Query("""
        SELECT p FROM Product p 
        WHERE (LOWER(p.name.uz) LIKE LOWER(CONCAT('%', :keyword, '%')) 
            OR LOWER(p.name.ru) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.name.en) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND p.count > 0 
        AND p.deleted = false
    """)
    fun searchAvailableProducts(@Param("keyword") keyword: String): List<Product>

    @Query("""
        SELECT p FROM Product p 
        WHERE p.category.id =: categoryId 
        AND p.count > 0 
        AND p.deleted = false
    """)
    fun findAvailableProductsByCategory(@Param("categoryId") categoryId: Long): List<Product>

    fun findByCountGreaterThanAndDeletedFalse(minCount: Long): List<Product>

}


@Repository
interface TransactionItemRepository : BaseRepository<TransactionItem> {

    fun findByTransactionId(transactionId: Long): List<TransactionItem>

    @Query("SELECT ti FROM TransactionItem ti WHERE ti.transaction.user.id = :userId")
    fun findByTransactionUserId(@Param("userId") userId: Long): List<TransactionItem>
}