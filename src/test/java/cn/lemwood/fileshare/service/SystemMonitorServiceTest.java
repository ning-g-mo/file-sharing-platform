package cn.lemwood.fileshare.service;

import cn.lemwood.fileshare.config.FileUploadConfig;
import cn.lemwood.fileshare.entity.FileInfo;
import cn.lemwood.fileshare.repository.FileInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 系统监控服务测试类
 * 
 * @author lemwood
 */
@ExtendWith(MockitoExtension.class)
class SystemMonitorServiceTest {

    @Mock
    private FileInfoRepository fileInfoRepository;

    @Mock
    private FileUploadConfig fileUploadConfig;

    @Mock
    private FileUploadConfig.Upload uploadConfig;

    @InjectMocks
    private SystemMonitorService systemMonitorService;

    @BeforeEach
    void setUp() {
        when(fileUploadConfig.getUpload()).thenReturn(uploadConfig);
        when(uploadConfig.getPath()).thenReturn(System.getProperty("java.io.tmpdir"));
    }

    @Test
    void testGetSystemStatus() {
        // Given
        when(fileInfoRepository.count()).thenReturn(5L);
        when(fileInfoRepository.getTotalFileSize()).thenReturn(1024000L);
        when(fileInfoRepository.findExpiredFiles(any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        // When
        Map<String, Object> status = systemMonitorService.getSystemStatus();

        // Then
        assertNotNull(status);
        assertEquals("healthy", status.get("status"));
        assertTrue(status.containsKey("timestamp"));
        assertTrue(status.containsKey("files"));
        assertTrue(status.containsKey("storage"));
        assertTrue(status.containsKey("memory"));
        
        verify(fileInfoRepository).count();
        verify(fileInfoRepository).getTotalFileSize();
    }

    @Test
    void testIsSystemHealthy_Healthy() {
        // Given
        when(fileInfoRepository.count()).thenReturn(1L);

        // When
        boolean isHealthy = systemMonitorService.isSystemHealthy();

        // Then
        assertTrue(isHealthy);
        verify(fileInfoRepository).count();
    }

    @Test
    void testIsSystemHealthy_DatabaseError() {
        // Given
        when(fileInfoRepository.count()).thenThrow(new RuntimeException("Database error"));

        // When
        boolean isHealthy = systemMonitorService.isSystemHealthy();

        // Then
        assertFalse(isHealthy);
        verify(fileInfoRepository).count();
    }

    @Test
    void testCleanExpiredFiles() {
        // Given
        List<FileInfo> expiredFiles = new ArrayList<>();
        FileInfo expiredFile = new FileInfo();
        expiredFile.setId(1L);
        expiredFile.setOriginalName("expired.txt");
        expiredFile.setFilePath("/tmp/expired.txt");
        expiredFiles.add(expiredFile);
        
        when(fileInfoRepository.findExpiredFiles(any(LocalDateTime.class)))
                .thenReturn(expiredFiles);

        // When
        int deletedCount = systemMonitorService.cleanExpiredFiles();

        // Then
        assertEquals(1, deletedCount);
        verify(fileInfoRepository).findExpiredFiles(any(LocalDateTime.class));
        verify(fileInfoRepository).delete(expiredFile);
    }

}