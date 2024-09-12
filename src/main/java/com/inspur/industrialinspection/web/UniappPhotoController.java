package com.inspur.industrialinspection.web;

import com.inspur.industrialinspection.entity.UniappPhoto;
import com.inspur.industrialinspection.service.UniappPhotoService;
import com.inspur.result.Result;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;

import java.util.Iterator;

/**
 * 图片上传
 * @author wangzhaodi
 * @date 2022/11/10 16:10
 */
@RestController
@RequestMapping(path="/industrial_robot/uniappPhoto")
@Api(tags = {"上传图片"})
@Slf4j
public class UniappPhotoController {

    @Autowired
    private UniappPhotoService uniappPhotoService;

    @PostMapping("/add")
    @ResponseBody
    @ApiOperation(value="上传图片", notes="")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result add(@Validated UniappPhoto uniappPhoto, MultipartRequest request) throws Exception {
        Iterator<String> fileNames = request.getFileNames();
        MultipartFile file = null;
        while (fileNames.hasNext()) {
            file = request.getFile(fileNames.next());
        }
        uniappPhotoService.add(uniappPhoto, file);
        return Result.success("");
    }

}
