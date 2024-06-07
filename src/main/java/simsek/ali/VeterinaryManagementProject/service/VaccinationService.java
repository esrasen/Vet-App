package simsek.ali.VeterinaryManagementProject.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import simsek.ali.VeterinaryManagementProject.dto.request.VaccinationRequest;
import simsek.ali.VeterinaryManagementProject.dto.response.VaccinationResponse;
import simsek.ali.VeterinaryManagementProject.entity.Animal;
import simsek.ali.VeterinaryManagementProject.entity.Report;
import simsek.ali.VeterinaryManagementProject.entity.Vaccination;
import simsek.ali.VeterinaryManagementProject.exception.DuplicateDataException;
import simsek.ali.VeterinaryManagementProject.exception.EntityNotFoundException;
import simsek.ali.VeterinaryManagementProject.exception.ProtectionStillActiveException;
import simsek.ali.VeterinaryManagementProject.repository.AnimalRepository;
import simsek.ali.VeterinaryManagementProject.repository.ReportRepository;
import simsek.ali.VeterinaryManagementProject.repository.VaccinationRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VaccinationService {

    private final VaccinationRepository vaccinationRepository;
    private final ReportRepository reportRepository;
    private final AnimalRepository animalRepository;
    private final ModelMapper modelMapper;


    public Page<VaccinationResponse> findAllVaccinations(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return vaccinationRepository.findAll(pageable)
                .map(vaccination -> modelMapper.map(vaccination, VaccinationResponse.class));
    }

    public VaccinationResponse findVaccinationById(Long id) {
        Vaccination vaccination = vaccinationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id, Vaccination.class));
        return modelMapper.map(vaccination, VaccinationResponse.class);
    }

    public Page<VaccinationResponse> findAnimalsByVaccinationProtectionFinishDateRange(LocalDate startDate, LocalDate endDate, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return vaccinationRepository.findByProtectionFinishDateBetween(startDate, endDate, pageable)
                .map(vaccination -> modelMapper.map(vaccination, VaccinationResponse.class));
    }

    public Page<VaccinationResponse> findVaccinationsByAnimalName(String name, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return vaccinationRepository.findByAnimalNameContainingIgnoreCase(name, pageable)
                .map(vaccination -> modelMapper.map(vaccination, VaccinationResponse.class));
    }

    public VaccinationResponse createVaccination(VaccinationRequest vaccinationRequest) {
        List<Vaccination> existValidVaccinationWithSameSpecsAnd =
                vaccinationRepository.findByNameAndCodeAndAnimalIdAndProtectionFinishDateGreaterThanEqual(
                        vaccinationRequest.getName(), vaccinationRequest.getCode(),
                        vaccinationRequest.getAnimalWithoutCustomer().getId(),
                        vaccinationRequest.getProtectionStartDate());

        if (!existValidVaccinationWithSameSpecsAnd.isEmpty()) {
            throw new ProtectionStillActiveException("Girdiğiniz aşı koruyuculuk tarihlerinde zaten devam eden bir koruyucuk var. Lütfen koruyuculuk tarihini değiştirin.");
        }

        Animal animal = animalRepository.findById(vaccinationRequest.getAnimalWithoutCustomer().getId())
                .orElseThrow(() -> new EntityNotFoundException(vaccinationRequest.getAnimalWithoutCustomer().getId(), Animal.class));

        Vaccination newVaccination = new Vaccination();
        newVaccination.setName(vaccinationRequest.getName());
        newVaccination.setCode(vaccinationRequest.getCode());
        newVaccination.setProtectionStartDate(vaccinationRequest.getProtectionStartDate());
        newVaccination.setProtectionFinishDate(vaccinationRequest.getProtectionFinishDate());
        newVaccination.setAnimal(animal);

        if (vaccinationRequest.getReportId() != null) {
            Report report = reportRepository.findById(vaccinationRequest.getReportId())
                    .orElseThrow(() -> new EntityNotFoundException(vaccinationRequest.getReportId(), Report.class));
            newVaccination.setReport(report);
        }

        return modelMapper.map(vaccinationRepository.save(newVaccination), VaccinationResponse.class);
    }

    @Transactional
    public VaccinationResponse updateVaccination(Long id, VaccinationRequest vaccinationRequest) {
        Vaccination vaccination = vaccinationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id, Vaccination.class));

        boolean animalChanged = false;
        if (vaccinationRequest.getAnimalWithoutCustomer() != null) {
            Animal newAnimal = animalRepository.findById(vaccinationRequest.getAnimalWithoutCustomer().getId())
                    .orElseThrow(() -> new EntityNotFoundException(vaccinationRequest.getAnimalWithoutCustomer().getId(), Animal.class));
            if (!vaccination.getAnimal().getId().equals(newAnimal.getId())) {
                animalChanged = true;
                vaccination.setAnimal(newAnimal);
            }
        }

        boolean protectionDatesChanged = !vaccination.getProtectionStartDate().equals(vaccinationRequest.getProtectionStartDate()) ||
                !vaccination.getProtectionFinishDate().equals(vaccinationRequest.getProtectionFinishDate());

        if (protectionDatesChanged || animalChanged) {
            List<Vaccination> existOtherValidVaccinationFromRequest =
                    vaccinationRepository.findByNameAndCodeAndAnimalIdAndProtectionFinishDateGreaterThanEqual(
                            vaccinationRequest.getName(), vaccinationRequest.getCode(),
                            vaccinationRequest.getAnimalWithoutCustomer().getId(),
                            vaccinationRequest.getProtectionStartDate());

            if (!existOtherValidVaccinationFromRequest.isEmpty() &&
                    !existOtherValidVaccinationFromRequest.get(existOtherValidVaccinationFromRequest.size() - 1).getId().equals(id)) {
                throw new DuplicateDataException(Vaccination.class);
            }

            if (!existOtherValidVaccinationFromRequest.isEmpty()) {
                throw new ProtectionStillActiveException("Girdiğiniz aşı koruyuculuk tarihlerinde zaten devam eden bir koruyuculuk var. Lütfen koruyuculuk tarihini değiştirin.");
            }
        }


        if (vaccinationRequest.getReportId() != null) {
            Report report = reportRepository.findById(vaccinationRequest.getReportId())
                    .orElseThrow(() -> new EntityNotFoundException(vaccinationRequest.getReportId(), Report.class));
            vaccination.setReport(report);
        } else {
            vaccination.setReport(null);
        }

        vaccination.setName(vaccinationRequest.getName());
        vaccination.setCode(vaccinationRequest.getCode());
        vaccination.setProtectionStartDate(vaccinationRequest.getProtectionStartDate());
        vaccination.setProtectionFinishDate(vaccinationRequest.getProtectionFinishDate());

        return modelMapper.map(vaccinationRepository.save(vaccination), VaccinationResponse.class);
    }


    public String deleteVaccination(Long id) {
        Vaccination vaccination = vaccinationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id, Vaccination.class));
        vaccinationRepository.delete(vaccination);
        return "Vaccine deleted.";
    }
}
