package com.dsec.backend.service.async;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncServiceImpl implements AsyncService {

  // Asynchronous Service for SlashCommands
  @Async("taskExecutor")
  public void runCommands(Runnable r){
    r.run();
  }

}
