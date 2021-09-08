package com.github.marlonflorencio.grpc.blog.client;

import com.proto.blog.Blog;
import com.proto.blog.BlogRequest;
import com.proto.blog.BlogResponse;
import com.proto.blog.BlogServiceGrpc;
import com.proto.blog.BlogId;
import com.proto.blog.ListBlogRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class BlogClient {

    public static void main(String[] args) {
        System.out.println("Hello I'm a gRPC client for Blog");

        BlogClient main = new BlogClient();
        main.run();
    }

    private void run() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);

        Blog blog = Blog.newBuilder()
                .setAuthorId("John")
                .setTitle("New blog!")
                .setContent("Hello world this is my first blog!")
                .build();

        BlogResponse createResponse = blogClient.createBlog(
                BlogRequest.newBuilder()
                        .setBlog(blog)
                        .build()
        );

        System.out.println("Received create blog response");
        System.out.println(createResponse.toString());

        String blogId = createResponse.getBlog().getId();

        System.out.println("Reading blog....");

        BlogResponse readBlogResponse = blogClient.readBlog(BlogId.newBuilder()
                .setBlogId(blogId)
                .build());

        System.out.println(readBlogResponse.toString());

        // trigger a not found error
//        System.out.println("Reading blog with non existing id....");
//
//        ReadBlogResponse readBlogResponseNotFound = blogClient.readBlog(ReadBlogRequest.newBuilder()
//                .setBlogId("5bd83b5cf476074e2bff3495")
//                .build());

        Blog newBlog = Blog.newBuilder()
                .setId(blogId)
                .setAuthorId("Changed Author")
                .setTitle("New blog (updated)!")
                .setContent("Hello world this is my first blog! I've added some more content")
                .build();

        System.out.println("Updating blog...");
        BlogResponse updateBlogResponse = blogClient.updateBlog(
                BlogRequest.newBuilder().setBlog(newBlog).build());

        System.out.println("Updated blog");
        System.out.println(updateBlogResponse.toString());

        System.out.println("Deleting blog");
        BlogId deleteBlogResponse = blogClient.deleteBlog(
                BlogId.newBuilder().setBlogId(blogId).build()
        );

        System.out.println("Deleted blog");

//        System.out.println("Reading blog....");
//        // this one should return NOT_FOUND
//        ReadBlogResponse readBlogResponseAfterDeletion = blogClient.readBlog(ReadBlogRequest.newBuilder()
//                .setBlogId(blogId)
//                .build());

        // we list the blogs in our database
        blogClient.listBlog(ListBlogRequest.newBuilder().build()).forEachRemaining(
                listBlogResponse -> System.out.println(listBlogResponse.getBlog())
        );

    }
}
