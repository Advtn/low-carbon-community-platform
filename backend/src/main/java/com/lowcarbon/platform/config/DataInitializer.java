package com.lowcarbon.platform.config;

import com.lowcarbon.platform.entity.BehaviorRule;
import com.lowcarbon.platform.entity.MallItem;
import com.lowcarbon.platform.entity.User;
import com.lowcarbon.platform.enums.UserRole;
import com.lowcarbon.platform.repository.BehaviorRuleRepository;
import com.lowcarbon.platform.repository.MallItemRepository;
import com.lowcarbon.platform.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(UserRepository userRepository,
                               BehaviorRuleRepository ruleRepository,
                               MallItemRepository itemRepository) {
        return args -> {
            seedUsers(userRepository);
            seedRules(ruleRepository);
            seedItems(itemRepository);
        };
    }

    private void seedUsers(UserRepository userRepository) {
        LocalDateTime now = LocalDateTime.now();
        upsertUser(userRepository, "admin", "admin123", "\u793e\u533a\u7ba1\u7406\u5458", UserRole.ADMIN, now.minusDays(120));
        upsertUser(userRepository, "alice", "123456", "\u5f20\u4e09", UserRole.RESIDENT, now.minusDays(90));
        upsertUser(userRepository, "bob", "123456", "\u674e\u56db", UserRole.RESIDENT, now.minusDays(88));
        upsertUser(userRepository, "carol", "123456", "\u738b\u4e94", UserRole.RESIDENT, now.minusDays(85));
        upsertUser(userRepository, "david", "123456", "\u8d75\u516d", UserRole.RESIDENT, now.minusDays(83));
    }

    private void seedRules(BehaviorRuleRepository ruleRepository) {
        upsertRule(ruleRepository, 1L,
                "\u6b65\u884c\u6216\u9a91\u884c\u51fa\u884c",
                "\u65e5\u5e38\u901a\u52e4\u9009\u62e9\u6b65\u884c\u6216\u9a91\u884c\u7b49\u4f4e\u78b3\u65b9\u5f0f",
                10,
                0.8,
                5);
        upsertRule(ruleRepository, 2L,
                "\u5783\u573e\u5206\u7c7b\u6295\u653e",
                "\u5b8c\u6210\u4e00\u6b21\u5783\u573e\u5206\u7c7b\u5e76\u6b63\u786e\u6295\u653e",
                6,
                0.3,
                6);
        upsertRule(ruleRepository, 3L,
                "\u81ea\u5e26\u6c34\u676f",
                "\u5916\u51fa\u6d88\u8d39\u65f6\u4f7f\u7528\u81ea\u5e26\u6c34\u676f",
                8,
                0.4,
                4);
        upsertRule(ruleRepository, 4L,
                "\u7a7a\u8c0326\u5ea6\u8bbe\u7f6e",
                "\u7a7a\u8c03\u6e29\u5ea6\u8bbe\u7f6e\u5728 26 \u5ea6\u53ca\u4ee5\u4e0a",
                5,
                0.2,
                3);
    }

    private void seedItems(MallItemRepository itemRepository) {
        upsertItem(itemRepository, 1L, "\u73af\u4fdd\u5e06\u5e03\u888b", "\u53ef\u91cd\u590d\u4f7f\u7528\u8d2d\u7269\u888b", 100, 120);
        upsertItem(itemRepository, 2L, "\u4e0d\u9508\u94a2\u5438\u7ba1\u5957\u88c5", "\u53ef\u5faa\u73af\u4f7f\u7528\u5438\u7ba1\u5957\u88c5", 180, 60);
        upsertItem(itemRepository, 3L, "\u518d\u751f\u7eb8\u7b14\u8bb0\u672c", "\u518d\u751f\u7eb8\u73af\u4fdd\u8bb0\u4e8b\u672c", 60, 80);
        upsertItem(itemRepository, 4L, "\u7af9\u7ea4\u7ef4\u7259\u5237", "\u53ef\u964d\u89e3\u65e5\u7528\u54c1", 20, 200);
        upsertItem(itemRepository, 5L, "\u592a\u9633\u80fd\u5c0f\u53f0\u706f", "USB \u5145\u7535\u592a\u9633\u80fd\u53f0\u706f", 220, 40);
    }

    private void upsertUser(UserRepository repository,
                            String username,
                            String password,
                            String nickname,
                            UserRole role,
                            LocalDateTime createdAt) {
        User user = repository.findByUsername(username).orElseGet(User::new);
        if (user.getId() == null) {
            user.setUsername(username);
            user.setCreatedAt(createdAt);
        }
        user.setPassword(password);
        user.setNickname(nickname);
        user.setRole(role);
        repository.save(user);
    }

    private void upsertRule(BehaviorRuleRepository repository,
                            Long id,
                            String name,
                            String description,
                            int pointsPerAction,
                            double carbonReductionPerAction,
                            int dailyLimit) {
        BehaviorRule rule = repository.findById(id).orElseGet(BehaviorRule::new);
        if (rule.getId() == null) {
            rule.setId(id);
        }
        rule.setName(name);
        rule.setDescription(description);
        rule.setPointsPerAction(pointsPerAction);
        rule.setCarbonReductionPerAction(carbonReductionPerAction);
        rule.setDailyLimit(dailyLimit);
        rule.setActive(true);
        repository.save(rule);
    }

    private void upsertItem(MallItemRepository repository,
                            Long id,
                            String name,
                            String description,
                            int pointsCost,
                            int stock) {
        MallItem item = repository.findById(id).orElseGet(MallItem::new);
        if (item.getId() == null) {
            item.setId(id);
        }
        item.setName(name);
        item.setDescription(description);
        item.setPointsCost(pointsCost);
        item.setStock(stock);
        item.setEnabled(true);
        repository.save(item);
    }
}
