package com.stonewu.sitepush.reconciler;

import com.stonewu.sitepush.service.PushService;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.plugin.SettingFetcher;

/**
 * Reconciler for {@link Category}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@Slf4j
public class PostPushReconciler extends AbstractPushReconciler
    implements Reconciler<Reconciler.Request> {

    public PostPushReconciler(SettingFetcher settingFetcher, ReactiveExtensionClient client,
        PushService pushService) {
        super(settingFetcher, client, pushService);
    }

    @Override
    public Result reconcile(Request request) {
        PublishExtension publishExtension = client.fetch(Post.class, request.name())
            .blockOptional()
            .map(PostAdapter::new).orElseThrow();
        return reconcile(publishExtension);
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new Post())
            .build();
    }

    private record PostAdapter(Post post) implements PublishExtension {

        @Override
        public boolean isPublished() {
            return post.isPublished();
        }

        @Override
        public String getSlug() {
            return post.getSpec().getSlug();
        }

        @Override
        public String getPermalink() {
            return post.getStatusOrDefault().getPermalink();
        }

        @Override
        public String getKind() {
            return post.getKind();
        }

        @Override
        public boolean isVisible() {
            return post.getSpec().getVisible().equals(Post.VisibleEnum.PUBLIC);
        }
        @Override
        public boolean isObserved() {
            if(post.getMetadata() != null && post.getStatusOrDefault() != null){
                return post.getMetadata().getVersion().equals(post.getStatusOrDefault().getObservedVersion());
            }
            return false;
        }
    }
}
