package model

type Prefs struct {
        Port             string `yaml:"port"`
        SnapshotsDir     string `yaml:"snapshots_dir"`
        VersionFile      string `yaml:"version_file"`
        Username         string `yaml:"username"`
        Password         string `yaml:"password"`
        DbUsername       string `yaml:"db_username"`
        DbPassword       string `yaml:"db_password"`
}