## Grundprovisionering med Ansible

    ansible-playbook -i [inventory-fil] provision.yml
    
Exempelvis:

    ansible-playbook -i hosts_lab provision.yml
    
Kom ihåg att git-crypt behöver installeras separat:

    ansible-playbook -i hosts_lab provision_git-crypt.yml
    
    