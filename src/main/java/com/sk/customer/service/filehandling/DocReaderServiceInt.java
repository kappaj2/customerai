package com.sk.customer.service.filehandling;

import com.sk.customer.dto.ReadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface DocReaderServiceInt {
     ReadResponse readFile(MultipartFile file) throws Exception;
}
