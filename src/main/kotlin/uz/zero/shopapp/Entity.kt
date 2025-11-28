package uz.zero.shopapp

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Table
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import jakarta.persistence.Transient
import org.hibernate.annotations.ColumnDefault
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.util.Date

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
class BaseEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @CreatedDate @Temporal(TemporalType.TIMESTAMP) var createdDate: Date? = null,
    @LastModifiedDate @Temporal(TemporalType.TIMESTAMP) var modifiedDate: Date? = null,
    @CreatedBy var createdBy: String? = null,
    @LastModifiedBy var lastModifiedBy: String? = null,
    @Column(nullable = false) @ColumnDefault(value = "false") var deleted: Boolean = false,
)


@Entity
@Table(name = "categories")
class Category(
    @Column(nullable = false) var name: String,
    @Column(name = "orders" , nullable = false) var order: Long,
) : BaseEntity()


@Entity
@Table(name = "products")
class Product(
    @Column(nullable = false) var name: LocalizedName,
    @Column(nullable = false) var count: Long,
    @Column(nullable = false) var amount: BigDecimal,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "category_id", nullable = false) var category: Category,
) : BaseEntity()

@Entity
@Table(name = "users")
class User(
    @Column(nullable = false) var fullname: String,
    @Column(unique = true, nullable = false) var username: String,
    @Column(nullable = false) var balance: BigDecimal = BigDecimal.ZERO
) : BaseEntity()

@Entity
@Table(name = "transactions")
class Transaction(
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) var user: User,
    @Column(name = "total_amount", nullable = false) var totalAmount: BigDecimal,
    @Temporal(TemporalType.TIMESTAMP) @Column(nullable = false)var date: Date = Date()
) : BaseEntity()

@Entity
@Table(name = "transaction_items")
class TransactionItem(
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id", nullable = false) var product: Product,
    @Column(nullable = false)var count: Long,
    @Column(nullable = false)var amount: BigDecimal,
    @Column(name = "total_amount", nullable = false) var totalAmount: BigDecimal,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "transaction_id", nullable = false)
    var transaction: Transaction
) : BaseEntity()

@Entity
@Table(name = "user_payment_transactions")
class UserPaymentTransaction(
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) var user: User,
    @Column(nullable = false) var amount: BigDecimal,
    @Temporal(TemporalType.TIMESTAMP) @Column(nullable = false) var date: Date = Date()
) : BaseEntity()




@Embeddable
class LocalizedName(
    @Column(length = 50) var uz: String,
    @Column(length = 50) var ru: String,
    @Column(length = 50) var en: String,
) {
    @Transient
    fun localized(): String {
        return when (LocaleContextHolder.getLocale().language) {
            "en" -> this.en
            "ru" -> this.ru
            else -> this.uz
        }
    }
}