package com.inspur.industrialinspection.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.druid.util.StringUtils;
import com.inspur.industrialinspection.dao.ParkInfoDao;
import com.inspur.industrialinspection.dao.RobotInfoDao;
import com.inspur.industrialinspection.dao.RobotRoomDao;
import com.inspur.industrialinspection.dao.RoomInfoDao;
import com.inspur.industrialinspection.entity.RobotInfo;
import com.inspur.industrialinspection.entity.RobotRoom;
import com.inspur.industrialinspection.service.RequestService;
import com.inspur.industrialinspection.service.RobotInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;
/**
 * 机器人服务实现
 * @author kliu
 * @date 2022/6/7 16:10
 */
@Service
public class RobotInfoServiceImpl implements RobotInfoService {

    @Autowired
    private RobotInfoDao robotInfoDao;
    @Autowired
    private RoomInfoDao roomInfoDao;
    @Autowired
    private RobotRoomDao robotRoomDao;
    @Value("${param.parent.path}")
    private String paramParentPath;
    @Value("${param.filename.robotBasic}")
    private String robotBasic;
    @Value("${param.filename.robot}")
    private String paramRobotFilename;
    @Autowired
    private RequestService requestService;
    @Autowired
    private ParkInfoDao parkInfoDao;

    @Override
    public List list(long roomId){
        int parkId = requestService.getParkIdByToken();
        return robotInfoDao.list(parkId, roomId);
    }

    /**
     * 获取机器人列表-dcim
     * @param parkId
     * @param jsonObject
     * @return java.util.List
     * @author kliu
     * @date 2022/9/1 17:08
     */
    @Override
    public List getRobotInfos(int parkId, JSONObject jsonObject) {
        long roomId = 0;
        if (jsonObject.containsKey("roomId")){
            roomId = jsonObject.getLong("roomId");
        }
        List<RobotInfo> list = robotInfoDao.list(parkId, roomId);
        for (RobotInfo robotInfo : list) {
            robotInfo.setInUse("");
        }
        return list;
    }


    @Override
    public List listWithoutPark(){
        return robotInfoDao.listWithoutPark();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(JSONObject jsonObject){

        //jsonObject 转化为RobotRoom结构化数据
        Long robotId = jsonObject.getLong("robotId");
        String robotName = jsonObject.getStr("robotName");
        Long roomId = jsonObject.getLong("roomId");

        RobotInfo robotInfo = new RobotInfo();
        robotInfo.setRobotId(robotId);
        robotInfo.setRobotName(robotName);

        RobotRoom robotRoom = new RobotRoom();
        robotRoom.setRoomId(roomId);
        robotRoom.setRobotInfo(robotInfo);

        if(StringUtils.isEmpty(robotRoom.getRobotInfo().getRobotName())){
            throw new RuntimeException("机器人名称不能为空，请检查");
        }
        if(!roomInfoDao.checkExist(robotRoom.getRoomId())){
            throw new RuntimeException("传入的机房id对应的数据不存在，请检查");
        }
        if(robotInfoDao.checkIsExist(robotId)){
            throw new RuntimeException("传入的机器人id已存在，请检查");
        }
        robotInfoDao.add(robotRoom.getRobotInfo());
        robotRoomDao.add(robotRoom.getRoomId(), robotId);

        int parkId = requestService.getParkIdByToken();

        //添加机器人配置文件生成
        //读取机器人的配置文件作为基准
        String basicFileName = paramParentPath+robotBasic;
        basicFileName = basicFileName.replace("{parkpinyin}\\\\","");

        File file = new File(basicFileName);
        if (!file.exists()){
            return;
        }

        String fileStr = FileUtil.readUtf8String(basicFileName);

        String newFileName = paramParentPath+paramRobotFilename;
        newFileName = newFileName.replace("","").replace("{robotid}", robotId+"").replace("{parkpinyin}",parkInfoDao.getDetlById(parkId).getParkPinyin());

        FileUtil.writeUtf8String(fileStr, newFileName);

    }
}
