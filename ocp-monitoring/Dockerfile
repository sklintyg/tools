FROM iron/base
EXPOSE 8080

# Define non-root user
USER 1000:1000

# Add binary + GUI
ADD dist/ocpmonitoring-linux-amd64 /
ADD gui /gui

ENTRYPOINT ["./ocpmonitoring-linux-amd64"]
