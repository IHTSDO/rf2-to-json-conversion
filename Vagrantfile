# -*- mode: ruby -*-
# vi: set ft=ruby :

# All Vagrant configuration is done below. The "2" in Vagrant.configure
# configures the configuration version (we support older styles for
# backwards compatibility). Please don't change it unless you know what
# you're doing.
Vagrant.configure(2) do |config|
  # The most common configuration options are documented and commented below.
  # For a complete reference, please see the online documentation at
  # https://docs.vagrantup.com.

  # Every Vagrant development environment requires a box. You can search for
  # boxes at https://atlas.hashicorp.com/search.
  config.vm.provider :parallels do |_, override|
    override.vm.box = "parallels/ubuntu-14.04"
  end

  config.vm.provider :virtualbox do |_, override|
    override.vm.box = "ubuntu/trusty64"
  end

  # Disable automatic box update checking. If you disable this, then
  # boxes will only be checked for updates when the user runs
  # `vagrant box outdated`. This is not recommended.
  # config.vm.box_check_update = false

  # Create a forwarded port mapping which allows access to a specific port
  # within the machine from a port on the host machine. In the example below,
  # accessing "localhost:8080" will access port 80 on the guest machine.
  # config.vm.network "forwarded_port", guest: 80, host: 8080

  # Create a private network, which allows host-only access to the machine
  # using a specific IP.
  # config.vm.network "private_network", ip: "192.168.33.10"

  # Create a public network, which generally matched to bridged network.
  # Bridged networks make the machine appear as another physical device on
  # your network.
  # config.vm.network "public_network"

  # Share an additional folder to the guest VM. The first argument is
  # the path on the host to the actual folder. The second argument is
  # the path on the guest to mount the folder. And the optional third
  # argument is a set of non-required options.
  # config.vm.synced_folder "../data", "/vagrant_data"
  hostos = RbConfig::CONFIG["host_os"].downcase
  case hostos
  when 'linux-gnu'
    config.vm.synced_folder ".", "/vagrant", :nfs => true,
    :mount_options => ['rw', 'vers=3', 'tcp', 'nordirplus', 'nolock', 'local_lock=none']
  else
    config.vm.synced_folder ".", "/vagrant", :nfs => true
  end

  # You need to have the bindfs plugin installed.
  # You can run this via:
  #
  #   vagrant plugin install vagrant-bindfs
  #
  config.bindfs.bind_folder "/vagrant", "/nfs/host",
                        :perms => "u=rwx:g=rwx:o=rwx",
                        :create_as_user => true

  # Provider-specific configuration so you can fine-tune various
  # backing providers for Vagrant. These expose provider-specific options.
  # rf2 conversion requires a lot of resources and will fail if they
  # are not available.
  config.vm.provider :virtualbox do |vbox|
    # Use VBoxManage to customize the VM. For example to change memory:
    vbox.customize ["modifyvm", :id, "--memory", "2048"]
  end

  # if parallels provider available, set those values instead
  config.vm.provider :parallels do |par|
    par.memory = "4096"
    par.cpus = 2
  end

  config.vm.provision :shell, path: "scripts/provision.sh", args: ENV['SHELL_ARGS']

  # View the documentation for the provider you are using for more
  # information on available options.

  # Define a Vagrant Push strategy for pushing to Atlas. Other push strategies
  # such as FTP and Heroku are also available. See the documentation at
  # https://docs.vagrantup.com/v2/push/atlas.html for more information.
  # config.push.define "atlas" do |push|
  #   push.app = "YOUR_ATLAS_USERNAME/YOUR_APPLICATION_NAME"
  # end

  # Enable provisioning with a shell script. Additional provisioners such as
  # Puppet, Chef, Ansible, Salt, and Docker are also available. Please see the
  # documentation for more information about their specific syntax and use.
  # config.vm.provision "shell", inline: <<-SHELL
  #   sudo apt-get update
  #   sudo apt-get install -y apache2
  # SHELL
end
