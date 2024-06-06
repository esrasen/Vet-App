package simsek.ali.VeterinaryManagementProject.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import simsek.ali.VeterinaryManagementProject.dto.request.ReportRequest;
import simsek.ali.VeterinaryManagementProject.dto.response.ReportResponse;
import simsek.ali.VeterinaryManagementProject.dto.response.VaccinationResponse;
import simsek.ali.VeterinaryManagementProject.entity.Appointment;
import simsek.ali.VeterinaryManagementProject.entity.Report;
import simsek.ali.VeterinaryManagementProject.exception.DuplicateDataException;
import simsek.ali.VeterinaryManagementProject.exception.EntityAlreadyExistException;
import simsek.ali.VeterinaryManagementProject.exception.EntityNotFoundException;
import simsek.ali.VeterinaryManagementProject.repository.ReportRepository;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final AppointmentService appointmentService;
    private final ModelMapper modelMapper;

    public Page<ReportResponse> findAllReports(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return reportRepository.findAll(pageable).map(this::reportResponseDtoFromReport);
    }

    public ReportResponse findReportById(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id, Report.class));
        return reportResponseDtoFromReport(report);
    }

    public ReportResponse reportResponseDtoFromReport(Report report) {
        ReportResponse reportResponse = modelMapper.map(report, ReportResponse.class);
        if (report.getAppointment() != null && report.getAppointment().getAnimal() != null && report.getAppointment().getAnimal().getCustomer() != null) {
            reportResponse.getAppointment().setCustomerName(report.getAppointment().getAnimal().getCustomer().getName());
        } else {
            reportResponse.getAppointment().setCustomerName(null);
        }
        return reportResponse;
    }

    public ReportResponse createReport(ReportRequest reportRequest) {
        Optional<Report> existReportWithSameSpecs = reportRepository.findByAppointmentId(reportRequest.getAppointmentId());

        if (existReportWithSameSpecs.isPresent()) {
            throw new EntityAlreadyExistException(Report.class);
        }

        Appointment appointmentFromDb = appointmentService.findAppointmenById(reportRequest.getAppointmentId());
        reportRequest.setAppointmentId(null);
        Report newReport = modelMapper.map(reportRequest, Report.class);
        newReport.setAppointment(appointmentFromDb);
        return convertToDto(reportRepository.save(newReport));
    }

    public ReportResponse updateReport(Long id, ReportRequest reportRequest) {
        Optional<Report> reportFromDb = reportRepository.findById(id);
        Optional<Report> existOtherReportFromRequest =
                reportRepository.findReportByTitleAndDiagnosisAndPriceAndAppointmentId(reportRequest.getTitle(), reportRequest.getDiagnosis(), reportRequest.getPrice(), reportRequest.getAppointmentId());

        if (reportFromDb.isEmpty()) {
            throw new EntityNotFoundException(id, Report.class);
        }

        if (existOtherReportFromRequest.isPresent() && !existOtherReportFromRequest.get().getId().equals(id)) {
            throw new DuplicateDataException(Report.class);
        }

        Appointment appointmentFromDb = appointmentService.findAppointmenById(reportRequest.getAppointmentId());
        Report updatedReport = reportFromDb.get();
        updatedReport.setTitle(reportRequest.getTitle());
        updatedReport.setDiagnosis(reportRequest.getDiagnosis());
        updatedReport.setPrice(reportRequest.getPrice());
        updatedReport.setAppointment(appointmentFromDb);
        return convertToDto(reportRepository.save(updatedReport));
    }

    public String deleteReport(Long id) {
        Optional<Report> reportFromDb = reportRepository.findById(id);
        if (reportFromDb.isEmpty()) {
            throw new EntityNotFoundException(id, Report.class);
        } else {
            reportRepository.delete(reportFromDb.get());
            return "Report deleted.";
        }
    }

    private ReportResponse convertToDto(Report report) {
        ReportResponse response = modelMapper.map(report, ReportResponse.class);
        if (report.getVaccinationList() != null) {
            response.setVaccinationList(report.getVaccinationList().stream()
                    .map(vaccination -> modelMapper.map(vaccination, VaccinationResponse.class))
                    .collect(Collectors.toList()));
        }
        return response;
    }
}
