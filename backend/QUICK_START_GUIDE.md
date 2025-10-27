# Quick Start Guide - PagoDirecto CRM/ERP

**For Developers Continuing This Implementation**

---

## Project Status Overview

✅ **1 Module Fully Complete:** Clientes (production-ready)
🟡 **5 Modules Domain Complete:** Oportunidades, Tareas, Productos, Ventas, Reportes

**Next Task:** Complete application layers (DTOs, services, controllers) for the 5 remaining modules.

---

## How to Complete a Module (Follow Clientes Pattern)

### Step 1: Create DTOs
**Location:** `{module}/src/main/java/com/pagodirecto/{module}/application/dto/`

**Example:** `ClienteDTO.java`
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteDTO {
    private UUID id;

    @NotNull(message = "Campo obligatorio")
    @NotBlank(message = "No puede estar vacío")
    private String campo;

    // ... more fields with validation
}
```

**Key Points:**
- Add `@NotNull`, `@NotBlank`, `@Email`, `@Size` validations
- Include all entity fields except collections (unless needed)
- Add created_at, created_by, updated_at, updated_by fields

---

### Step 2: Create MapStruct Mapper
**Location:** `{module}/src/main/java/com/pagodirecto/{module}/application/mapper/`

**Example:** `ClienteMapper.java`
```java
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClienteMapper {
    ClienteDTO toDTO(Cliente entity);
    Cliente toEntity(ClienteDTO dto);
    List<ClienteDTO> toDTOList(List<Cliente> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntityFromDTO(ClienteDTO dto, @MappingTarget Cliente entity);
}
```

**Key Points:**
- Use `componentModel = "spring"` for Spring injection
- Use `@MappingTarget` for update operations
- Ignore audit fields when updating

---

### Step 3: Create Repository
**Location:** `{module}/src/main/java/com/pagodirecto/{module}/infrastructure/repository/`

**Example:** `ClienteRepository.java`
```java
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, UUID> {
    Optional<Cliente> findByCodigo(String codigo);

    Page<Cliente> findByStatus(ClienteStatus status, Pageable pageable);

    @Query("SELECT c FROM Cliente c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :term, '%'))")
    Page<Cliente> search(@Param("term") String term, Pageable pageable);

    // Use JOIN FETCH to prevent N+1
    @Query("SELECT c FROM Cliente c LEFT JOIN FETCH c.contactos WHERE c.id = :id")
    Optional<Cliente> findByIdWithContactos(@Param("id") UUID id);
}
```

**Key Points:**
- Extend `JpaRepository<Entity, UUID>`
- Add custom query methods
- Use `@Query` with JOIN FETCH to prevent N+1 queries
- Add `Page<>` return types for pagination

---

### Step 4: Create Service Interface
**Location:** `{module}/src/main/java/com/pagodirecto/{module}/application/service/`

**Example:** `ClienteService.java`
```java
public interface ClienteService {
    ClienteDTO crear(ClienteDTO dto, UUID usuarioId);
    ClienteDTO actualizar(UUID id, ClienteDTO dto, UUID usuarioId);
    ClienteDTO buscarPorId(UUID id);
    Page<ClienteDTO> listarTodos(Pageable pageable);
    void eliminar(UUID id);
    // ... custom business methods
}
```

---

### Step 5: Create Service Implementation
**Location:** `{module}/src/main/java/com/pagodirecto/{module}/application/service/impl/`

**Example:** `ClienteServiceImpl.java`
```java
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository repository;
    private final ClienteMapper mapper;

    @Override
    @Transactional
    public ClienteDTO crear(ClienteDTO dto, UUID usuarioId) {
        log.info("Creando entidad: {}", dto);

        // Validate business rules
        // ...

        Cliente entity = mapper.toEntity(dto);
        entity.setCreatedBy(usuarioId);
        entity.setUpdatedBy(usuarioId);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());

        Cliente saved = repository.save(entity);
        log.info("Entidad creada con ID: {}", saved.getId());

        return mapper.toDTO(saved);
    }

    @Override
    @Transactional
    public ClienteDTO actualizar(UUID id, ClienteDTO dto, UUID usuarioId) {
        Cliente entity = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("No encontrado"));

        mapper.updateEntityFromDTO(dto, entity);
        entity.setUpdatedBy(usuarioId);
        entity.setUpdatedAt(Instant.now());

        return mapper.toDTO(repository.save(entity));
    }

    @Override
    public ClienteDTO buscarPorId(UUID id) {
        return repository.findById(id)
            .map(mapper::toDTO)
            .orElseThrow(() -> new IllegalArgumentException("No encontrado"));
    }

    @Override
    public Page<ClienteDTO> listarTodos(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    @Override
    @Transactional
    public void eliminar(UUID id) {
        Cliente entity = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("No encontrado"));
        repository.delete(entity); // Soft delete via @SQLDelete
    }
}
```

**Key Points:**
- `@Transactional(readOnly = true)` on class
- `@Transactional` on write methods
- Use `@Slf4j` for logging
- Set audit fields (created_by, updated_by, timestamps)
- Throw `IllegalArgumentException` for not found (or create custom exceptions)

---

### Step 6: Create REST Controller
**Location:** `{module}/src/main/java/com/pagodirecto/{module}/api/controller/`

**Example:** `ClienteController.java`
```java
@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Clientes", description = "API de gestión de clientes")
public class ClienteController {

    private final ClienteService service;

    @Operation(summary = "Crear cliente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Creado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PostMapping
    public ResponseEntity<ClienteDTO> crear(
            @Valid @RequestBody ClienteDTO dto,
            @AuthenticationPrincipal UserDetails user) {

        UUID usuarioId = UUID.randomUUID(); // TODO: Get from security context
        ClienteDTO created = service.crear(dto, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Actualizar cliente")
    @PutMapping("/{id}")
    public ResponseEntity<ClienteDTO> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ClienteDTO dto,
            @AuthenticationPrincipal UserDetails user) {

        UUID usuarioId = UUID.randomUUID(); // TODO: Get from security context
        return ResponseEntity.ok(service.actualizar(id, dto, usuarioId));
    }

    @Operation(summary = "Obtener por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ClienteDTO> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @Operation(summary = "Listar todos")
    @GetMapping
    public ResponseEntity<Page<ClienteDTO>> listar(
            @PageableDefault(size = 20, sort = "nombre") Pageable pageable) {
        return ResponseEntity.ok(service.listarTodos(pageable));
    }

    @Operation(summary = "Eliminar")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
```

**Key Points:**
- Use `@Tag` for OpenAPI grouping
- Use `@Operation` and `@ApiResponses` for documentation
- Use `@Valid` for DTO validation
- Use `@PageableDefault` for pagination
- Return proper HTTP status codes (201, 200, 204, 404, etc.)

---

## Common Patterns

### Pattern 1: One-to-Many Relationship
**Example:** Cliente → Contactos

**Entity:**
```java
@OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
private Set<Contacto> contactos = new HashSet<>();

public void agregarContacto(Contacto contacto) {
    contacto.setCliente(this);
    this.contactos.add(contacto);
}
```

**Service Method:**
```java
public ContactoDTO agregarContacto(UUID clienteId, ContactoDTO dto, UUID usuarioId) {
    Cliente cliente = clienteRepository.findById(clienteId)
        .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

    Contacto contacto = contactoMapper.toEntity(dto);
    cliente.agregarContacto(contacto); // Use domain method
    contacto.setCreatedBy(usuarioId);

    clienteRepository.save(cliente);
    return contactoMapper.toDTO(contacto);
}
```

---

### Pattern 2: Status Transitions
**Example:** Cotizacion: BORRADOR → ENVIADA → ACEPTADA

**Entity:**
```java
public void enviar() {
    if (CotizacionStatus.BORRADOR.equals(this.status)) {
        this.status = CotizacionStatus.ENVIADA;
        this.updatedAt = Instant.now();
    } else {
        throw new IllegalStateException("Solo se pueden enviar cotizaciones en borrador");
    }
}
```

**Service:**
```java
@Transactional
public CotizacionDTO enviar(UUID id, UUID usuarioId) {
    Cotizacion cotizacion = repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("No encontrada"));

    cotizacion.enviar(); // Domain method handles validation
    cotizacion.setUpdatedBy(usuarioId);

    return mapper.toDTO(repository.save(cotizacion));
}
```

**Controller:**
```java
@PutMapping("/{id}/enviar")
public ResponseEntity<CotizacionDTO> enviar(
        @PathVariable UUID id,
        @AuthenticationPrincipal UserDetails user) {
    UUID usuarioId = UUID.randomUUID(); // TODO: Get from context
    return ResponseEntity.ok(service.enviar(id, usuarioId));
}
```

---

### Pattern 3: Calculation in Domain
**Example:** ItemCotizacion calculations

**Entity:**
```java
@PrePersist
@PreUpdate
protected void onSave() {
    calcularMontos();
}

public void calcularMontos() {
    this.subtotal = cantidad.multiply(precioUnitario);

    if (descuentoPorcentaje.compareTo(BigDecimal.ZERO) > 0) {
        this.descuentoMonto = subtotal.multiply(descuentoPorcentaje)
            .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
    }

    this.total = subtotal.subtract(descuentoMonto);
}
```

---

### Pattern 4: Search with Multiple Criteria
**Repository:**
```java
@Query("SELECT c FROM Cliente c WHERE " +
       "(:nombre IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
       "(:status IS NULL OR c.status = :status)")
Page<Cliente> buscarConFiltros(
    @Param("nombre") String nombre,
    @Param("status") ClienteStatus status,
    Pageable pageable
);
```

**Service:**
```java
public Page<ClienteDTO> buscarConFiltros(String nombre, ClienteStatus status, Pageable pageable) {
    return repository.buscarConFiltros(nombre, status, pageable)
        .map(mapper::toDTO);
}
```

**Controller:**
```java
@GetMapping("/search")
public ResponseEntity<Page<ClienteDTO>> buscar(
        @RequestParam(required = false) String nombre,
        @RequestParam(required = false) ClienteStatus status,
        @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(service.buscarConFiltros(nombre, status, pageable));
}
```

---

## Module-Specific Implementation Notes

### Oportunidades
- Implement `calcularValorPonderado()` in service layer
- Pipeline stage transitions require validation
- Activity management (add, complete)

### Tareas
- Polymorphic relations: validate `relacionado_tipo` exists
- Comment threading
- Due date notifications (future: scheduled job)

### Productos
- **Complex:** Category tree queries (recursive CTE)
- **Complex:** Price calculation algorithm:
  ```java
  PrecioProducto findApplicablePrice(
      UUID productoId,
      String segmento,
      Integer cantidad,
      LocalDate fecha
  )
  ```
- Stock management with optimistic locking

### Ventas
- **Complex:** Calculation engine for totals
- **Complex:** Quote → Order conversion:
  ```java
  PedidoDTO convertirCotizacionAPedido(UUID cotizacionId, UUID usuarioId)
  ```
- Inventory reservation on order confirmation
- Delivery tracking with partial deliveries

### Reportes
- **Complex:** KPI calculation services
- Dashboard JSONB configuration validation
- Widget rendering logic (frontend will consume)

---

## Testing Strategy

### Unit Tests
**Location:** `src/test/java/.../service/`

```java
@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository repository;

    @Mock
    private ClienteMapper mapper;

    @InjectMocks
    private ClienteServiceImpl service;

    @Test
    void crear_Success() {
        // Arrange
        ClienteDTO dto = ClienteDTO.builder().nombre("Test").build();
        Cliente entity = new Cliente();
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repository.save(any())).thenReturn(entity);
        when(mapper.toDTO(entity)).thenReturn(dto);

        // Act
        ClienteDTO result = service.crear(dto, UUID.randomUUID());

        // Assert
        assertNotNull(result);
        verify(repository).save(entity);
    }
}
```

### Integration Tests
**Location:** `src/test/java/.../controller/`

```java
@SpringBootTest
@AutoConfigureMockMvc
class ClienteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void crear_Success() throws Exception {
        ClienteDTO dto = ClienteDTO.builder()
            .nombre("Test")
            .tipo(ClienteTipo.PERSONA)
            .build();

        mockMvc.perform(post("/api/v1/clientes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nombre").value("Test"));
    }
}
```

---

## Build & Run Commands

### Compile & Build
```bash
cd /Users/lperez/Workspace/Development/next/crm_pd/backend
./mvnw clean install
```

### Run Application
```bash
cd application
../mvnw spring-boot:run
```

### Run Tests
```bash
../mvnw test
```

### Run Single Module Tests
```bash
cd clientes
../mvnw test
```

---

## Troubleshooting

### Problem: MapStruct mapper not generated
**Solution:** Ensure annotation processor is configured in `pom.xml`:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct-processor</artifactId>
                <version>1.5.5.Final</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

Then rebuild: `./mvnw clean install`

---

### Problem: Circular dependency between modules
**Solution:** Check dependency order in parent `pom.xml`:
```xml
<modules>
    <module>core-domain</module>
    <module>seguridad</module>
    <module>clientes</module>
    <!-- ... other modules ... -->
    <module>application</module>
</modules>
```

---

### Problem: Lazy loading exception
**Solution:** Use JOIN FETCH in repository query:
```java
@Query("SELECT c FROM Cliente c LEFT JOIN FETCH c.contactos WHERE c.id = :id")
Optional<Cliente> findByIdWithContactos(@Param("id") UUID id);
```

---

## Estimated Time to Complete

| Module | DTOs | Mapper | Repository | Service | Controller | Tests | Total |
|--------|------|--------|------------|---------|------------|-------|-------|
| Oportunidades | 1h | 0.5h | 1h | 1.5h | 1h | 2h | 6-8h |
| Tareas | 0.5h | 0.5h | 1h | 1h | 1h | 2h | 5-6h |
| Productos | 1h | 0.5h | 2h | 2h | 1.5h | 3h | 8-10h |
| Ventas | 1.5h | 1h | 1.5h | 3h | 2h | 4h | 12-14h |
| Reportes | 1h | 0.5h | 1h | 3h | 1.5h | 3h | 8-10h |

**Total Estimated Time:** 40-50 hours

---

## Resources

- **Clientes Module:** Full reference implementation
- **Database Schema:** `/backend/application/src/main/resources/db/migration/V1__initial_schema.sql`
- **OpenAPI Docs:** http://localhost:8080/swagger-ui.html (when running)
- **MapStruct Docs:** https://mapstruct.org/documentation/stable/reference/html/
- **Spring Data JPA:** https://docs.spring.io/spring-data/jpa/docs/current/reference/html/

---

## Next Steps Checklist

- [ ] Complete Oportunidades module (6-8h)
- [ ] Complete Tareas module (5-6h)
- [ ] Complete Productos module (8-10h)
- [ ] Complete Ventas module (12-14h)
- [ ] Complete Reportes module (8-10h)
- [ ] Write unit tests for all services
- [ ] Write integration tests for all controllers
- [ ] Create global exception handler
- [ ] Integrate with Seguridad module
- [ ] Configure OpenAPI UI
- [ ] Performance testing
- [ ] Deploy to development environment

---

**Good luck with the implementation!**

For questions, refer to:
- `/backend/FINAL_IMPLEMENTATION_REPORT.md` - Comprehensive documentation
- `/backend/CLAUDE.md` - Project guidelines
- `/backend/clientes/` - Full reference implementation
