package simsek.ali.VeterinaryManagementProject.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import simsek.ali.VeterinaryManagementProject.entity.Animal;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface AnimalRepository extends JpaRepository<Animal, Long> {
    Optional<Animal> findByNameAndSpeciesAndGenderAndDateOfBirth(String name, String species, String gender, LocalDate dateOfBirth);

    Page<Animal> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Animal> findByCustomer_NameContainingIgnoreCase(String customerName, Pageable pageable);


}
