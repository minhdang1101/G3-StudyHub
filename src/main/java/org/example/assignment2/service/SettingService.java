package org.example.assignment2.service;

import org.example.assignment2.dto.SettingDTO;
import org.example.assignment2.model.Setting;
import org.example.assignment2.repository.SettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SettingService {
    @Autowired
    private  SettingRepository settingRepo;

    public SettingDTO getSettingDtoById(Integer id) {
        Setting s = settingRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Setting not found with id: " + id));

        SettingDTO dto = new SettingDTO();
        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setValue(s.getValue());
        dto.setOrderIndex(s.getOrderIndex());
        dto.setStatus(s.getStatus());
        dto.setDescription(s.getDescription());
        if (s.getParent() != null) {
            dto.setTypeId(s.getParent().getId());
        }

        return dto;
    }

    @Transactional
    public void saveSetting(SettingDTO dto) {
        Setting setting;
        if (dto.getId() != null) {
            setting = settingRepo.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Setting not found for update"));
        } else {
            setting = new Setting();
        }

        setting.setName(dto.getName());
        setting.setValue(dto.getValue());
        setting.setOrderIndex(dto.getOrderIndex());
        setting.setStatus(dto.getStatus());
        setting.setDescription(dto.getDescription());
        setting.setTypeId(dto.getTypeId());

        settingRepo.save(setting);
    }

    public List<Setting> getSettings(Integer typeId, String status, String keyword, String sortField, String sortDir) {
        Sort sort = Sort.by(sortField);
        sort = "asc".equalsIgnoreCase(sortDir) ? sort.ascending() : sort.descending();

        String searchStatus = (status != null && !status.isEmpty()) ? status : null;
        String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;

        return settingRepo.filterAndSearch(typeId, searchStatus, searchKeyword, sort);
    }
    public List<Setting> getAllTypes() {
        return settingRepo.findByParentIsNull();
    }

    public List<Setting> getSettingsByTypeId(Integer typeId) {
        return settingRepo.findByTypeId(typeId);
    }

    public Setting getSettingById(Integer id) {
        return settingRepo.findById(id).orElse(null);
    }
}