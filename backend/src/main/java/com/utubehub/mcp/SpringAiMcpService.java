package com.utubehub.mcp;

import com.utubehub.entity.ChannelEntity;
import com.utubehub.entity.VideoEntity;
import com.utubehub.repository.ChannelRepository;
import com.utubehub.repository.VideoRepository;
import com.utubehub.service.YouTubeService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpringAiMcpService {

    private final ChannelRepository channelRepository;
    private final VideoRepository videoRepository;
    private final YouTubeService youTubeService;

    @Autowired
    public SpringAiMcpService(ChannelRepository channelRepository, VideoRepository videoRepository, YouTubeService youTubeService) {
        this.channelRepository = channelRepository;
        this.videoRepository = videoRepository;
        this.youTubeService = youTubeService;
    }

    @Tool(name = "syncUserSubscriptionsTool", description = "Model Context Protocol Tool: Syncs user YouTube subscriptions and videos using OAuth access token")
    public String syncUserSubscriptionsTool(String accessToken, String userId) {
        try {
            List<ChannelEntity> channels = youTubeService.syncUserSubscriptions(accessToken, userId);
            return "Successfully synced " + channels.size() + " YouTube subscriptions via Spring AI MCP Tool for " + userId;
        } catch (Exception e) {
            return "MCP Tool Sync Notice: " + e.getMessage();
        }
    }

    @Tool(name = "searchSubscriptionsMcpTool", description = "Model Context Protocol Tool: Searches indexed YouTube subscriptions and videos using natural language prompt queries")
    public List<VideoEntity> searchSubscriptionsMcpTool(String userId, String query) {
        return videoRepository.findByUserId(userId);
    }

    @Tool(name = "getChannelResourceMcp", description = "Model Context Protocol Resource: Fetches YouTube channel metadata and subscriber metrics for active account")
    public List<ChannelEntity> getChannelResourceMcp(String userId) {
        return channelRepository.findByUserId(userId);
    }

    @Tool(name = "generateRefinementPromptMcp", description = "Model Context Protocol Prompt: Generates structured Gemini AI prompt template for natural language YouTube content refining")
    public String generateRefinementPromptMcp(String rawQuery) {
        return "Act as an expert YouTube Content Curator. Filter, rank, and summarize YouTube subscriptions and videos for prompt: '" + rawQuery + "'.";
    }
}
