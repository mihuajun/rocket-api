package com.github.alenfive.rocketapi.function;

import com.github.alenfive.rocketapi.entity.vo.IgnoreWrapper;
import com.github.alenfive.rocketapi.extend.ApiInfoContent;
import com.github.alenfive.rocketapi.service.ScriptParseService;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 默认全局静态函数
 */
@Component
public class UtilsFunction implements IFunction{

    @Autowired
    private ApiInfoContent apiInfoContent;

    @Autowired
    private ScriptParseService scriptParseService;

    @Autowired
    private HttpServletResponse response;

    @Override
    public String getVarName() {
        return "Utils";
    }

    /**
     * 获取上下文中的指定变量
     * @param varName
     */
    public Object val(String varName){
        return scriptParseService.buildParamItem(apiInfoContent.getApiParams(),varName);
    }

    /**
     * 无返回结构体方法
     * @param data
     * @return
     */
    public IgnoreWrapper ignoreWrapper(Object data){
        return new IgnoreWrapper(data);
    }

    /**
     * 文件下载
     * @param fileName
     * @param inputStream
     */
    public ResponseEntity<InputStreamResource> download(String fileName, InputStream inputStream) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.parse("attachment; filename="+fileName));
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        InputStreamResource inputStreamResource = new InputStreamResource(inputStream);
        return new ResponseEntity(inputStreamResource, headers, HttpStatus.OK);
    }

    /**
     * 图片在线预览
     * @param inputStream
     * @return
     */
    public ResponseEntity<InputStreamResource> preview(InputStream inputStream) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        InputStreamResource inputStreamResource = new InputStreamResource(inputStream);
        return new ResponseEntity(inputStreamResource, headers, HttpStatus.OK);
    }
}
