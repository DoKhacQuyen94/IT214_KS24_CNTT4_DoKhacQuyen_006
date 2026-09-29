# CẨM NANG TOÀN DIỆN: CÁCH LÀM BÀI THI MICROSERVICES (SPRING CLOUD)
> **Tài liệu hướng dẫn thực chiến:** Áp dụng cho mọi đề thi Microservices sử dụng Spring Boot, Spring Cloud, OpenFeign, Resilience4j, Redis và Kafka (dù đề tài thay đổi thành Đặt phòng khách sạn, Đăng ký khóa học, Đặt vé máy bay, Bán hàng, v.v.).

---

## MỤC LỤC
1. [Bản chất kiến trúc đề thi Microservices (Mô hình 5 thành phần)](#1-bản-chất-kiến-trúc-đề-thi-microservices)
2. [Checklist 5 phút đầu giờ: Cách "đọc vị" đề thi và nhặt manh mối](#2-checklist-5-phút-đầu-giờ-cách-đọc-vị-đề-thi)
3. [Chuẩn bị hạ tầng: File `docker-compose.yml` chạy mọi bài](#3-chuẩn-bị-hạ-tầng-file-docker-composeyml)
4. [Chi tiết từng bước triển khai (Code mẫu chuẩn mực)](#4-chi-tiết-từng-bước-triển-khai)
   - [Bước 1: Service quản lý tài nguyên + Redis Cache](#bước-1-service-tài-nguyên--redis-cache)
   - [Bước 2: Service thông báo + Kafka Consumer & Mail](#bước-2-service-thông-báo--kafka-consumer--mail)
   - [Bước 3: Service nghiệp vụ - OpenFeign & Resilience4j Circuit Breaker](#bước-3-gọi-liên-dịch-vụ-feign--circuit-breaker)
   - [Bước 4: Service nghiệp vụ - Xử lý đơn & Kafka Producer](#bước-4-xử-lý-nghiệp-vụ-chính--bắn-kafka)
5. [Thứ tự khởi động hệ thống & Test thông luồng bằng Postman](#5-thứ-tự-khởi-động-hệ-thống--test-thông-luồng)
6. [Bảng tra cứu lỗi kinh điển trong phòng thi & Cách xử lý](#6-bảng-tra-cứu-lỗi-kinh-điển)

---

## 1. BẢN CHẤT KIẾN TRÚC ĐỀ THI MICROSERVICES

Dù đề thi đổi sang bất kỳ chủ đề nào, 99% các đề thi Spring Cloud đều tuân theo mô hình **5 Service chuẩn**:

| STT | Service | Vai trò thực tế | Port mặc định | Bạn có cần code không? |
|---|---|---|---|---|
| **1** | `eureka-server` | Tổng đài danh bạ (Service Discovery) | `8761` | **Không** (thường đã dựng sẵn 100%) |
| **2** | `api-gateway` | Cổng bảo vệ, định tuyến URL | `8080` | **Không** (thường đã cấu hình sẵn route) |
| **3** | `*-service` (Product / Course / Hotel...) | Quản lý đối tượng con, tích hợp Cache | `8081` | **Có** (Gắn Redis Cache) |
| **4** | `*-service` (Order / Enrollment / Booking...) | Xử lý giao dịch chính (tính tiền, gọi Feign, bắn Kafka) | `8082` | **Có** (Viết nhiều nhất: Feign, Fallback, lưu DB, Kafka) |
| **5** | `notify-service` / `notification-service` | Nhận tin ngầm và gửi Mail/SMS | `8083` | **Có** (Kafka Consumer + JavaMail) |

---

## 2. CHECKLIST 5 PHÚT ĐẦU GIỜ: CÁCH "ĐỌC VỊ" ĐỀ THI

Ngay khi nhận được source code của đề thi, **ĐỪNG VỘI GÕ CODE NGAY**. Hãy làm 4 bước soi manh mối này:

### Bước 2.1: Tìm tất cả các chỗ bị "đục lỗ"
Trong IntelliJ IDEA, nhấn tổ hợp phím **`Ctrl + Shift + F`** (Find in Files), tìm kiếm cụm từ:
```text
throw new UnsupportedOperationException
```
hoặc:
```text
// TODO
```
👉 *Toàn bộ các file hiện ra trong kết quả tìm kiếm chính là danh sách các hàm bạn bắt buộc phải hoàn thành.*

### Bước 2.2: Soi file `application.yaml` của từng Service
Mở lần lượt các file `src/main/resources/application.yaml` để nhặt các thông số:
1. **Ở Service con (Product/Course...):**
   * Tìm mục `spring.cache`: Xem `time-to-live` là bao lâu, `key-prefix` là gì.
   * Tìm mục `spring.data.redis`: Xem cổng (`port`), mật khẩu (`password`).
2. **Ở Service nghiệp vụ (Order/Booking...):**
   * Tìm mục `resilience4j.circuitbreaker.instances`: Xem **tên instance** là gì (ví dụ: `productService`).
   * Tìm mục `spring.kafka.producer`: Kiểm tra xem đã bật producer chưa.
3. **Ở Service thông báo (Notify):**
   * Tìm tên topic: xem mục `kafka.order-created-topic` (hoặc tương đương).
   * Tìm tên consumer group: xem mục `group-id`.

### Bước 2.3: Soi thư mục `test/`
Mở các file trong thư mục `src/test/java/...`:
* Thầy cô thường viết sẵn test để chấm tự động. Đọc code test sẽ biết thầy cô muốn:
  * Tên cache là gì (ví dụ: `assertEquals("products", cacheable.cacheNames()[0])`).
  * Key cache là gì (ví dụ: `assertEquals("#id", cacheable.key())`).
  * Có bắt buộc `sync = true` không.

### Bước 2.4: Soi thư mục `exceptions/`
Xem các class Exception có sẵn (ví dụ: `ResourceNotFoundException`, `ServiceUnavailableException`). Đề đã tạo sẵn lỗi nào thì trong code gọi Feign bạn **phải ném đúng lỗi đó**, vì `GlobalExceptionHandler` đã được cấu hình để bắt đúng lỗi đó trả về mã HTTP tương ứng.

---

## 3. CHUẨN BỊ HẠ TẦNG: FILE `docker-compose.yml`

Để ứng dụng chạy thật trên máy, cần 3 thành phần: MySQL, Redis, Kafka. Tạo file `docker-compose.yml` ở thư mục gốc của dự án:

```yaml
version: '3.8'

services:
  # 1. Cơ sở dữ liệu MySQL (Port 3307 để tránh đụng với MySQL cài sẵn ở 3306)
  mysql:
    image: mysql:8.0
    container_name: exam-mysql
    ports:
      - "3307:3306"
    environment:
      MYSQL_ROOT_PASSWORD: "rootpassword"
      MYSQL_USER: "root"
      MYSQL_PASSWORD: "123456"
    volumes:
      - mysql_data:/var/lib/mysql

  # 2. Redis Cache
  redis:
    image: redis:7-alpine
    container_name: exam-redis
    ports:
      - "6379:6379"
    # Thay password bằng password bạn tìm thấy trong application.yaml
    command: redis-server --requirepass rikkeiacademy

  # 3. Apache Kafka (chạy chế độ KRaft không cần Zookeeper)
  kafka:
    image: confluentinc/cp-kafka:7.5.0
    container_name: exam-kafka
    ports:
      - "9092:9092"
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: 'CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT'
      KAFKA_ADVERTISED_LISTENERS: 'PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092'
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_PROCESS_ROLES: 'broker,controller'
      KAFKA_CONTROLLER_QUORUM_VOTERS: '1@kafka:29093'
      KAFKA_LISTENERS: 'PLAINTEXT://0.0.0.0:29092,PLAINTEXT_HOST://0.0.0.0:9092,CONTROLLER://0.0.0.0:29093'
      KAFKA_INTER_BROKER_LISTENER_NAME: 'PLAINTEXT'
      KAFKA_CONTROLLER_LISTENER_NAMES: 'CONTROLLER'
      KAFKA_LOG_DIRS: '/tmp/kraft-combined-logs'
      CLUSTER_ID: 'MkU3OEVBNTcwNTJENDM2Qk'

volumes:
  mysql_data:
```

> 💡 **Cách bật:** Mở Terminal tại thư mục chứa file, gõ:
> ```bash
> docker compose up -d
> ```
> Gõ `docker ps` để kiểm tra cả 3 container đều đang ở trạng thái `Up`.

---

## 4. CHI TIẾT TỪNG BƯỚC TRIỂN KHAI

---

### BƯỚC 1: Service Tài nguyên + Redis Cache
*(Áp dụng cho `product-service`, `course-service`, `room-service`...)*

#### Việc 1: Kiểm tra Entity
Mở Entity (ví dụ: `Product.java`): Bắt buộc phải có `implements Serializable` vì Redis cần tuần tự hoá dữ liệu thành byte để lưu vào RAM.
```java
public class Product implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    // ... các trường dữ liệu
}
```

#### Việc 2: Gắn `@Cacheable` ở ServiceImpl
Mở `*ServiceImpl.java` (ví dụ: `ProductServiceImpl.java`), thêm annotation vào hàm lấy chi tiết theo ID:

```java
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    // Tên cache lấy từ file yaml hoặc test (thường là số nhiều: products, courses...)
    @Cacheable(cacheNames = "products", key = "#id", sync = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
```
* **Giải thích tham số:**
  * `cacheNames = "products"`: Vùng nhớ lưu trữ.
  * `key = "#id"`: Phân biệt các bản ghi theo ID truyền vào.
  * `sync = true`: Chống Cache Stampede (nhiều request đọc đồng thời khi cache rỗng).

>  **Cách kiểm tra:** Nhấn chuột phải vào file test `ProductServiceImplTest` chọn **Run Test** → Test báo xanh là bạn đã xong bước 1.

---

### BƯỚC 2: Service Thông báo + Kafka Consumer & Mail
*(Áp dụng cho `notify-service`, `notification-service`...)*

#### Việc 1: Hoàn thiện `EmailService.java`
Mở file `EmailService.java`. Thêm `@Service` và dùng `JavaMailSender`:

```java
package org.example.notifyservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String from;

    // Tiêm cấu hình mail 'from' từ application.yaml
    public EmailService(JavaMailSender mailSender, 
                        @Value("${notification.mail.from:${spring.mail.username:no-reply@shop.com}}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void sendOrderCreatedEmail(String recipient) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(recipient);
            message.setSubject("Thông báo: Đặt hàng thành công!");
            message.setText("Xin chào,\nĐơn hàng của bạn đã được ghi nhận trên hệ thống.\nCảm ơn bạn!");
            
            mailSender.send(message);
            System.out.println(">>> [MAIL SUCCESS] Đã gửi mail thành công đến: " + recipient);
        } catch (Exception e) {
            // Log ra console để không làm chết Consumer nếu chưa config SMTP thật
            System.err.println(">>> [MAIL FAILED] Không thể gửi email (chưa cấu hình SMTP thật): " + e.getMessage());
        }
    }
}
```

#### Việc 2: Hoàn thiện `OrderCreatedConsumer.java`
Mở file `OrderCreatedConsumer.java`. Thêm `@Component` và `@KafkaListener`:

```java
package org.example.notifyservice.consumer;

import org.example.notifyservice.service.EmailService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedConsumer {

    private final EmailService emailService;

    public OrderCreatedConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(
        topics = "${notification.kafka.order-created-topic:order-created}", 
        groupId = "${spring.kafka.consumer.group-id:notify-service}"
    )
    public void consume(String email) {
        System.out.println(">>> [KAFKA CONSUMER] Nhận được message từ topic: " + email);
        emailService.sendOrderCreatedEmail(email);
    }
}
```

---

### BƯỚC 3: Gọi liên dịch vụ (Feign + Circuit Breaker)
*(Áp dụng cho `ProductGatewayService.java` hoặc `*GatewayService.java` trong `order-service`)*

#### Bản chất:
* `order-service` cần gọi sang `product-service` để kiểm tra sản phẩm có tồn tại và giá bao nhiêu.
* Nếu `product-service` sập, Circuit Breaker phải ngắt và ném lỗi dự phòng.

#### Code chuẩn mực:
```java
package org.example.orderservice.models.services.impl;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.clients.ProductClient;
import org.example.orderservice.exceptions.ProductNotFoundException;
import org.example.orderservice.exceptions.ProductServiceException;
import org.example.orderservice.models.dto.responses.ProductResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductGatewayService {

    private final ProductClient productClient;

    // name: phải khớp với tên trong application.yaml (resilience4j.circuitbreaker.instances.<name>)
    // fallbackMethod: tên hàm dự phòng phía dưới
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductByIdFallback")
    public ProductResponse getProductById(Long productId) {
        try {
            return productClient.getProductById(productId);
        } catch (FeignException.NotFound e) {
            // Khi Feign trả về mã HTTP 404
            throw new ProductNotFoundException(productId);
        }
    }

    /**
     * QUY TẮC BẮT BUỘC CỦA HÀM FALLBACK:
     * 1. Cùng kiểu trả về (ProductResponse).
     * 2. Cùng danh sách tham số ban đầu (Long productId).
     * 3. Thêm tham số cuối cùng là Throwable (hoặc Exception).
     */
    public ProductResponse getProductByIdFallback(Long productId, Throwable throwable) {
        log.error("Fallback triggered for productId: {} due to: {}", productId, throwable.getMessage());
        
        // Nếu bản chất là lỗi không tìm thấy (404), hãy ném tiếp để GlobalExceptionHandler trả về 404
        if (throwable instanceof ProductNotFoundException) {
            throw (ProductNotFoundException) throwable;
        }
        
        // Nếu do mạng sập, timeout, circuit breaker OPEN -> ném lỗi 502/503
        throw new ProductServiceException("Product service is currently unavailable. Please try again later.", throwable);
    }
}
```

---

### BƯỚC 4: Xử lý nghiệp vụ chính & Bắn Kafka
*(Áp dụng cho `OrderServiceImpl.java` hoặc `*ServiceImpl.java`)*

#### Luồng xử lý nghiệp vụ chuẩn:
1. Nhận `CreateOrderRequest` từ Controller.
2. Duyệt qua từng item, gọi `productGatewayService.getProductById(productId)`.
3. Kiểm tra tính hợp lệ và tính `subtotal = price * quantity`, tính tổng `total`.
4. Tạo `Order` entity, lưu vào `OrderRepository`.
5. Tạo các `OrderDetail` entities, gán vào `Order`, lưu vào `OrderDetailRepository`.
6. Bắn thông tin vào Kafka topic bằng `kafkaTemplate.send(...)`.
7. Map kết quả sang `OrderResponse` và trả về.

#### Code mẫu hoàn chỉnh:
```java
package org.example.orderservice.models.services.impl;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.models.constants.OrderStatus;
import org.example.orderservice.models.dto.requests.CreateOrderDetailRequest;
import org.example.orderservice.models.dto.requests.CreateOrderRequest;
import org.example.orderservice.models.dto.responses.OrderDetailResponse;
import org.example.orderservice.models.dto.responses.OrderResponse;
import org.example.orderservice.models.dto.responses.ProductResponse;
import org.example.orderservice.models.entities.Order;
import org.example.orderservice.models.entities.OrderDetail;
import org.example.orderservice.models.repositories.OrderDetailRepository;
import org.example.orderservice.models.repositories.OrderRepository;
import org.example.orderservice.models.services.OrderService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductGatewayService productGatewayService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Tên topic thường lấy từ file config hoặc hardcode "order-created"
    private static final String ORDER_CREATED_TOPIC = "order-created";

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        double totalAmount = 0.0;
        
        // 1. Tạo entity Order trước
        Order order = Order.builder()
                .customerName(request.customerName())
                .status(OrderStatus.PENDING)
                .total(0.0)
                .build();
        order = orderRepository.save(order);

        List<OrderDetailResponse> itemResponses = new ArrayList<>();
        List<OrderDetail> orderDetailsToSave = new ArrayList<>();

        // 2. Duyệt qua từng sản phẩm đặt mua
        for (CreateOrderDetailRequest itemReq : request.items()) {
            // Gọi sang Product Service qua Circuit Breaker
            ProductResponse product = productGatewayService.getProductById(itemReq.productId());
            
            double unitPrice = product.price();
            double subtotal = unitPrice * itemReq.quantity();
            totalAmount += subtotal;

            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .productId(product.id())
                    .quantity(itemReq.quantity())
                    .unitPrice(unitPrice)
                    .build();
            orderDetailsToSave.add(detail);

            itemResponses.add(new OrderDetailResponse(
                    null, // id sẽ có sau khi save
                    product.id(),
                    product.name(),
                    itemReq.quantity(),
                    unitPrice,
                    subtotal
            ));
        }

        // Lưu danh sách OrderDetail vào Database
        orderDetailRepository.saveAll(orderDetailsToSave);

        // Cập nhật lại tổng tiền cho Order
        order.setTotal(totalAmount);
        orderRepository.save(order);

        // 3. Bắn event lên Kafka cho Notify Service
        // Payload có thể là email khách hàng hoặc chuỗi JSON
        kafkaTemplate.send(ORDER_CREATED_TOPIC, request.customerEmail());
        System.out.println(">>> [KAFKA PRODUCER] Đã bắn event tạo đơn tới topic: " + ORDER_CREATED_TOPIC);

        // 4. Trả về kết quả
        return new OrderResponse(
                order.getId(),
                order.getCustomerName(),
                order.getTotal(),
                order.getStatus(),
                itemResponses
        );
    }
}
```

---

## 5. THỨ TỰ KHỞI ĐỘNG HỆ THỐNG & TEST THÔNG LUỒNG

Khi đến bước chạy demo trực tiếp cho thầy cô chấm, hãy tuân thủ **thứ tự bật các cửa sổ** như sau:

```text
[Bật Hạ tầng Docker] -> docker compose up -d (MySQL, Redis, Kafka)
         ↓
[Bật Service 1] ------> EurekaServerApplication (Cổng 8761)
         ↓            (Đợi 10 giây cho Eureka lên hẳn)
[Bật Service 2] ------> ProductServiceApplication (Cổng 8081)
         ↓
[Bật Service 3] ------> OrderServiceApplication (Cổng 8082)
         ↓
[Bật Service 4] ------> NotifyServiceApplication (Cổng 8083)
         ↓
[Bật Service 5] ------> ApiGatewayApplication (Cổng 8080)
```

### Kiểm tra bằng trình duyệt & Postman:
1. Mở trình duyệt vào `http://localhost:8761`: Xem giao diện Eureka đã hiển thị danh sách 4 instances: `API-GATEWAY`, `PRODUCT-SERVICE`, `ORDER-SERVICE`, `NOTIFY-SERVICE` chưa.
2. Mở Postman gửi Request:
   * **Method:** `POST`
   * **URL:** `http://localhost:8080/api/v1/orders` *(gửi qua API Gateway)*
   * **Body (JSON):**
     ```json
     {
       "customerName": "Nguyen Van A",
       "customerEmail": "test@gmail.com",
       "items": [
         {
           "productId": 1,
           "quantity": 2
         }
       ]
     }
     ```
3. **Kết quả đạt chuẩn:**
   * Trả về HTTP Status: `201 CREATED`.
   * Body trả về có `id`, `total`, `status = PENDING`.
   * Cửa sổ console của `notify-service` in ra: `Nhận được message từ topic: test@gmail.com` và `Đã gửi mail thành công`.

---

## 6. BẢNG TRA CỨU LỖI KINH ĐIỂN TRONG PHÒNG THI

| Tình trạng lỗi | Nguyên nhân | Cách khắc phục trong 30 giây |
|---|---|---|
| `Port 8080/8081/8082 already in use` | Service cũ chưa tắt hẳn hoặc có tiến trình khác chiếm cổng. | Mở PowerShell gõ:<br>`Stop-Process -Id (Get-NetTCPConnection -LocalPort <CỔNG>).OwningProcess -Force` |
| `Connection refused: localhost:6379 / 9092 / 3307` | Chưa bật Redis, Kafka hoặc MySQL trong Docker. | Chạy lệnh `docker compose up -d` và kiểm tra lại `docker ps`. |
| `Fallback method not found` | Khai báo tên hàm fallback sai hoặc sai tham số. | Kiểm tra hàm fallback: Phải có cùng tham số với hàm chính + tham số cuối là `Throwable`. |
| `NotSerializableException` | Entity lưu vào Redis chưa implement `Serializable`. | Vào Entity thêm `implements Serializable` và `private static final long serialVersionUID = 1L;`. |
| Feign Client gọi sang bị lỗi `Load balancer does not contain an instance for the service...` | Service kia chưa kịp đăng ký lên Eureka. | Vào `http://localhost:8761` kiểm tra xem service đích đã hiện tên màu xanh chưa, đợi khoảng 30s để Eureka đồng bộ registry. |
| Kafka ném lỗi `SerializationException` | Phía gửi dùng Serializer khác với Deserializer phía nhận. | Nếu gửi chuỗi đơn giản (String), đảm bảo config `key-serializer` và `value-serializer` là `StringSerializer`. |

---
*Chúc bạn ôn luyện vững vàng và đạt điểm tuyệt đối trong bài thi Microservices!*
