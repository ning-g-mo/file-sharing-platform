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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 文件服务测试类
 * 
 * @author lemwood
 */
@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private FileInfoRepository fileInfoRepository;

    @Mock
    private FileUploadConfig fileUploadConfig;

    @InjectMocks
    private FileService fileService;

    private FileInfo testFileInfo;

    @BeforeEach
    void setUp() {
        testFileInfo = new FileInfo();
        testFileInfo.setId(1L);
        testFileInfo.setFileKey("test-key-123");
        testFileInfo.setOriginalName("test.txt");
        testFileInfo.setStoredName("stored-test.txt");
        testFileInfo.setFileSize(1024L);
        testFileInfo.setContentType("text/plain");
    }

    @Test
    void testGetFileInfo_ExistingFile() {
        // Given
        when(fileInfoRepository.findByFileKey("test-key-123"))
                .thenReturn(Optional.of(testFileInfo));

        // When
        Optional<FileInfo> result = fileService.getFileInfo("test-key-123");

        // Then
        assertTrue(result.isPresent());
        assertEquals("test.txt", result.get().getOriginalName());
        verify(fileInfoRepository).findByFileKey("test-key-123");
    }

    @Test
    void testGetFileInfo_NonExistingFile() {
        // Given
        when(fileInfoRepository.findByFileKey("non-existing-key"))
                .thenReturn(Optional.empty());

        // When
        Optional<FileInfo> result = fileService.getFileInfo("non-existing-key");

        // Then
        assertFalse(result.isPresent());
        verify(fileInfoRepository).findByFileKey("non-existing-key");
    }

    @Test
    void testGetSystemStats() {
        // Given
        when(fileInfoRepository.count()).thenReturn(10L);
        when(fileInfoRepository.getTotalFileSize()).thenReturn(1024000L);

        // When
        FileService.SystemStats stats = fileService.getSystemStats();

        // Then
        assertEquals(10L, stats.getTotalFiles());
        assertEquals(1024000L, stats.getTotalSize());
        verify(fileInfoRepository).count();
        verify(fileInfoRepository).getTotalFileSize();
    }

}