package com.inspur.industrialinspection.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.util.StringUtils;
import com.inspur.code.ParaKey;
import com.inspur.industrialinspection.dao.ParkInfoDao;
import com.inspur.industrialinspection.dao.RobotInfoDao;
import com.inspur.industrialinspection.dao.RoomInfoDao;
import com.inspur.industrialinspection.dao.UserInfoDao;
import com.inspur.industrialinspection.entity.RobotInfo;
import com.inspur.industrialinspection.entity.RoomInfo;
import com.inspur.industrialinspection.entity.UserInfo;
import com.inspur.industrialinspection.service.RequestService;
import com.inspur.industrialinspection.service.RoomInfoService;
import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 机房信息服务实现
 *
 * @author kliu
 * @date 2022/6/7 16:10
 */
@Service
public class RoomInfoServiceImpl implements RoomInfoService {

    @Autowired
    private RoomInfoDao roomInfoDao;
    @Autowired
    private UserInfoDao userInfoDao;
    @Autowired
    private RobotInfoDao robotInfoDao;
    @Autowired
    private RequestService requestService;
    @Autowired
    private ParkInfoDao parkInfoDao;
    @Value("${param.parent.path}")
    private String paramParentPath;
    @Value("${param.filename.roomBasic}")
    private String roomBasicParam;
    @Value("${param.filename.room}")
    private String paramRoomFilename;
    @Autowired
    private MinioClient minioClient;
    @Value("${minio.endpoint}")
    private String minioBasic;

    @Override
    public List list(long robotId, long buildingId) {
        int parkId = requestService.getParkIdByToken();
        return roomInfoDao.listWithBuilding(robotId, buildingId, parkId);
    }

    /**
     * 获取机房信息-dcim
     *
     * @param parkId
     * @param paramObject
     * @return java.util.List
     * @author kliu
     * @date 2022/9/1 16:37
     */
    @Override
    public List getRoomInfos(int parkId, JSONObject paramObject) {
        Long robotId = 0L;
        Long buildingId = 0L;
        if (paramObject.containsKey("robotId")) {
            robotId = paramObject.getLong("robotId");
        }
        if (paramObject.containsKey("buildingId")) {
            buildingId = paramObject.getLong("buildingId");
        }

        List<RoomInfo> roomInfos = roomInfoDao.listWithBuilding(robotId, buildingId, parkId);

        for (RoomInfo roomInfo : roomInfos) {
            roomInfo.setInUse("");
            roomInfo.setParkId(0);
            roomInfo.setRoomAddr("");
            roomInfo.setRobotId(0);
        }

        return roomInfos;
    }

    @Override
    public List listWithoutToken(long robotId, int parkId) {
        return roomInfoDao.list(robotId, parkId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(RoomInfo roomInfo) {
        if (StringUtils.isEmpty(roomInfo.getRoomName())) {
            throw new RuntimeException("机房名称不能为空，请检查");
        }
        long roomId = roomInfoDao.addAndReturnId(roomInfo);

        int parkId = requestService.getParkIdByToken();
        //添加机房配置文件生成
        //读取某个机房的配置文件作为基准
        String basicFileName = paramParentPath + roomBasicParam;
        basicFileName = basicFileName.replace("{parkpinyin}\\\\", "");

        File file = new File(basicFileName);
        if (!file.exists()) {
            return;
        }

        String fileStr = FileUtil.readUtf8String(basicFileName);
        JSONObject fileObject = JSONUtil.parseObj(fileStr);
        if (fileObject.containsKey(ParaKey.DETECTION_PARA)) {
            JSONArray detectionParaArray = fileObject.getJSONArray(ParaKey.DETECTION_PARA);
            for (int i = 0; i < detectionParaArray.size(); i++) {
                detectionParaArray.getJSONObject(i).set("room_id", roomId);
            }
        }

        String newFileName = paramParentPath + paramRoomFilename;
        newFileName = newFileName.replace("", "").replace("{roomid}", roomId + "").replace("{parkpinyin}", parkInfoDao.getDetlById(parkId).getParkPinyin());

        FileUtil.writeUtf8String(fileObject.toString(), newFileName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(RoomInfo roomInfo, MultipartFile file2) {
        if (StringUtils.isEmpty(roomInfo.getRoomName())) {
            throw new RuntimeException("机房名称不能为空，请检查");
        }
        long roomId = roomInfoDao.addAndReturnId(roomInfo);
        try {
            roomInfo.setRoomId(roomId);
            saveFrofile(file2, roomInfo);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        int parkId = requestService.getParkIdByToken();
        //添加机房配置文件生成
        //读取某个机房的配置文件作为基准
        String basicFileName = paramParentPath + roomBasicParam;
        basicFileName = basicFileName.replace("{parkpinyin}\\\\", "");

        File file = new File(basicFileName);
        if (!file.exists()) {
            return;
        }

        String fileStr = FileUtil.readUtf8String(basicFileName);
        JSONObject fileObject = JSONUtil.parseObj(fileStr);
        if (fileObject.containsKey(ParaKey.DETECTION_PARA)) {
            JSONArray detectionParaArray = fileObject.getJSONArray(ParaKey.DETECTION_PARA);
            for (int i = 0; i < detectionParaArray.size(); i++) {
                detectionParaArray.getJSONObject(i).set("room_id", roomId);
            }
        }

        String newFileName = paramParentPath + paramRoomFilename;
        newFileName = newFileName.replace("", "").replace("{roomid}", roomId + "").replace("{parkpinyin}", parkInfoDao.getDetlById(parkId).getParkPinyin());

        FileUtil.writeUtf8String(fileObject.toString(), newFileName);

    }

    public void saveFrofile(MultipartFile multipartFile, RoomInfo roomInfo) throws Exception {
        if(multipartFile.isEmpty())return;
        String imgType = multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf("."));
        InputStream in = multipartFile.getInputStream();
        String fileName =  IdUtil.simpleUUID() + imgType;
        minioClient.putObject("roomthumbnail", fileName, in, new PutObjectOptions(in.available(), -1));
        in.close();

        String profileUrl = minioBasic + "/roomthumbnail/" + fileName;

        roomInfo.setThumbnailUrl(profileUrl);
        roomInfoDao.update(roomInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(RoomInfo roomInfo) {
        if (StringUtils.isEmpty(roomInfo.getRoomName())) {
            throw new RuntimeException("机房名称不能为空，请检查");
        }
        roomInfoDao.update(roomInfo);
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(RoomInfo roomInfo, MultipartFile file) {
        if (StringUtils.isEmpty(roomInfo.getRoomName())) {
            throw new RuntimeException("机房名称不能为空，请检查");
        }
        roomInfoDao.update(roomInfo);
        try {
            saveFrofile(file, roomInfo);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(RoomInfo roomInfo) {
        roomInfoDao.delete(roomInfo.getRoomId());
    }

    @Override
    public List roomRobotUserList() {
        List list = new ArrayList();

        List<RoomInfo> roomInfos = roomInfoDao.list(0);
        List<UserInfo> userInfos = userInfoDao.list();

        Map map;

        int parkId = requestService.getParkIdByToken();
        for (RoomInfo roomInfo : roomInfos) {
            map = new HashMap(6);
            long roomId = roomInfo.getRoomId();
            String roomName = roomInfo.getRoomName();
            long robotId = 0;
            String robotName = null;

            List<RobotInfo> robotInfos = robotInfoDao.list(parkId, roomId);
            if (robotInfos.size() > 0) {
                robotId = robotInfos.get(0).getRobotId();
                robotName = robotInfos.get(0).getRobotName();
            }

            long userId = 0;
            String userName = "";

            for (UserInfo userInfo : userInfos) {
                long userInfoRoomId = userInfo.getRoomId();
                if (userInfoRoomId == roomId) {
                    userId = userInfo.getUserId();
                    userName = userInfo.getUserName();
                }
            }

            map.put("roomId", roomId);
            map.put("robotName", robotName);
            map.put("robotId", robotId);
            map.put("roomName", roomName);
            map.put("userId", userId);
            map.put("userName", userName);
            map.put("buildingId", roomInfo.getBuildingId());
            list.add(map);
        }

        return list;
    }
}
