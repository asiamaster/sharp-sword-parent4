package com.mxny.ss.sid.service;


import com.mxny.ss.sid.dto.SnowflakeId;

public interface SnowflakeIdConverter {
  long convert(SnowflakeId id);

  SnowflakeId convert(long id);
}
