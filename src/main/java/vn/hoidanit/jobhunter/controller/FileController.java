package vn.hoidanit.jobhunter.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Paths;

import vn.hoidanit.jobhunter.domain.response.file.ResUploadFileDTO;
import vn.hoidanit.jobhunter.service.FileService;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.util.error.StorageException;

@RestController
@RequestMapping("/api/v1")
public class FileController {

    @Value("${hoidanit.upload-file.base-uri}")
    private String baseURI;

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/files")
    @ApiMessage("Upload single file")
    public ResponseEntity<ResUploadFileDTO> upload(
            @RequestParam(name = "file", required = false) MultipartFile file,
            @RequestParam("folder") String folder

    ) throws IOException, StorageException {
        // skip validate
        if (file == null || file.isEmpty()) {
            throw new StorageException("File is empty. Please upload a file.");
        }
        String fileName = file.getOriginalFilename();
        List<String> allowedExtensions = Arrays.asList("pdf", "jpg", "jpeg", "png", "doc", "docx");
        boolean isValid = allowedExtensions.stream().anyMatch(item -> fileName.toLowerCase().endsWith(item));

        if (!isValid) {
            throw new StorageException("Invalid file extension. only allows " + allowedExtensions.toString());
        }
        // map folder: if client requested 'company' store under public/image/company
        String mappedFolder = "";
        if (folder != null && "company".equalsIgnoreCase(folder.trim())) {
            mappedFolder = "image/company";
        } else {
            mappedFolder = folder;
        }

        // create a directory if not exist (pass full path: baseURI + mappedFolder)
        this.fileService.createDirectory(baseURI + mappedFolder);

        // store file using mapped folder
        String uploadFile = this.fileService.store(file, mappedFolder);

        ResUploadFileDTO res = new ResUploadFileDTO(uploadFile, Instant.now());

        return ResponseEntity.ok().body(res);
    }

    @GetMapping("/files")
    @ApiMessage("Download a file")
    public ResponseEntity<Resource> download(
            @RequestParam(name = "fileName", required = false) String fileName,
            @RequestParam(name = "folder", required = false) String folder)
            throws StorageException, FileNotFoundException {
        if (fileName == null || folder == null) {
            throw new StorageException("Missing required params : (fileName or folder) in query params.");
        }

        // map folder same as upload (company -> image/company)
        String mappedFolder = "";
        if (folder != null && "company".equalsIgnoreCase(folder.trim())) {
            mappedFolder = "image/company";
        } else {
            mappedFolder = folder;
        }

        // check file exist (and not a directory)
        long fileLength = this.fileService.getFileLength(fileName, mappedFolder);
        if (fileLength == 0) {
            throw new StorageException("File with name = " + fileName + " not found.");
        }

        // download a file
        InputStreamResource resource = this.fileService.getResource(fileName, mappedFolder);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentLength(fileLength)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping("/public/images/{imageName}")
    @ApiMessage("View an image")
    public ResponseEntity<?> viewImage(@PathVariable(name = "imageName") String imageName) {
        try {
            java.nio.file.Path imagePath = Paths.get("public/image/company/" + imageName);
            UrlResource resource = new UrlResource(imagePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                // Xác định content type dựa trên extension
                String contentType = MediaType.IMAGE_JPEG_VALUE;
                if (imageName.toLowerCase().endsWith(".png")) {
                    contentType = MediaType.IMAGE_PNG_VALUE;
                } else if (imageName.toLowerCase().endsWith(".gif")) {
                    contentType = MediaType.IMAGE_GIF_VALUE;
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                // Nếu ảnh không tồn tại, trả về ảnh imagenotfound
                java.nio.file.Path notFoundPath = Paths.get("public/image/company/imagenotfound.png");
                UrlResource notFoundResource = new UrlResource(notFoundPath.toUri());

                if (notFoundResource.exists() && notFoundResource.isReadable()) {
                    return ResponseEntity.ok()
                            .contentType(MediaType.IMAGE_JPEG)
                            .body(notFoundResource);
                } else {
                    // Nếu cả imagenotfound.jpg cũng không có, trả về 404
                    return ResponseEntity.notFound().build();
                }
            }
        } catch (Exception e) {
            // Nếu có lỗi, cố gắng trả về imagenotfound
            try {
                java.nio.file.Path notFoundPath = Paths.get("public/image/company/imagenotfound.jpg");
                UrlResource notFoundResource = new UrlResource(notFoundPath.toUri());

                if (notFoundResource.exists() && notFoundResource.isReadable()) {
                    return ResponseEntity.ok()
                            .contentType(MediaType.IMAGE_JPEG)
                            .body(notFoundResource);
                }
            } catch (Exception ex) {
                // Ignore
            }
            return ResponseEntity.notFound().build();
        }
    }
}
